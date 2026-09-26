package dev.ryanhcode.sable.sublevel.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaterniond;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * The sub-level section draws of a single frame (or other render, like a diagram).
 * <p>
 * Sub-level sections are drawn with the vanilla terrain pipelines. Each section gets a vanilla {@code ChunkSection}
 * uniform block whose chunk position places its vertices relative to an anchor of the sub-level, and each sub-level gets
 * a transform uniform moving that anchor relative space into camera relative world space.
 */
@ApiStatus.Internal
public final class SubLevelSectionDraws {

    private final SubLevelRenderContext context;
    private final int cameraBlockX;
    private final int cameraBlockY;
    private final int cameraBlockZ;
    private final double cameraOffsetX;
    private final double cameraOffsetY;
    private final double cameraOffsetZ;
    private final int atlasWidth;
    private final int atlasHeight;
    private final List<DynamicUniforms.ChunkSectionInfo> sectionInfos = new ArrayList<>();
    private final List<Batch> batches = new ArrayList<>();
    private int maxIndicesRequired;
    private GpuBufferSlice[] sectionSlices;

    /**
     * @param context       How the sub-levels are rendered
     * @param globalCamera  The camera position the vanilla {@code Globals} uniform block was written with this frame
     * @param atlasWidth    The width of the block atlas
     * @param atlasHeight   The height of the block atlas
     */
    public SubLevelSectionDraws(final SubLevelRenderContext context, final Vec3 globalCamera, final int atlasWidth, final int atlasHeight) {
        this.context = context;
        this.cameraBlockX = Mth.floor(globalCamera.x);
        this.cameraBlockY = Mth.floor(globalCamera.y);
        this.cameraBlockZ = Mth.floor(globalCamera.z);
        this.cameraOffsetX = this.cameraBlockX - globalCamera.x;
        this.cameraOffsetY = this.cameraBlockY - globalCamera.y;
        this.cameraOffsetZ = this.cameraBlockZ - globalCamera.z;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
    }

    public SubLevelRenderContext getContext() {
        return this.context;
    }

    /**
     * Starts the draws of a sub-level.
     *
     * @param subLevel      The sub-level
     * @param pose          The pose to render the sub-level with
     * @param anchorX       The x coordinate of the plot space anchor, close to the geometry of the sub-level
     * @param anchorY       The y coordinate of the plot space anchor
     * @param anchorZ       The z coordinate of the plot space anchor
     * @param skyLightScale The scale of the sky light of the sub-level, in [0, 1]
     * @param disableFog    Whether fog should not be applied to the sub-level
     * @return The batch to add sections to
     */
    public Batch begin(final ClientSubLevel subLevel, final Pose3dc pose, final int anchorX, final int anchorY, final int anchorZ, final float skyLightScale, final boolean disableFog) {
        final Batch batch = new Batch(subLevel, anchorX, anchorY, anchorZ, skyLightScale, disableFog);

        final Vector3dc position = pose.position();
        final Vector3dc rotationPoint = pose.rotationPoint();
        final Vector3dc scale = pose.scale();

        // The vanilla terrain shader computes pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset from the
        // global camera, so with ChunkPosition = section - anchor + CameraBlockPos, pos = plot - anchor + CameraOffset.
        // The transform moves that into world space relative to the camera of this render.
        final double offsetX = this.cameraOffsetX;
        final double offsetY = this.cameraOffsetY;
        final double offsetZ = this.cameraOffsetZ;

        final Matrix4d transform = new Matrix4d()
                .translation(position.x() - this.context.cameraX(), position.y() - this.context.cameraY(), position.z() - this.context.cameraZ())
                .rotate(new Quaterniond(pose.orientation()))
                .scale(scale.x(), scale.y(), scale.z())
                .translate(anchorX - rotationPoint.x() - offsetX, anchorY - rotationPoint.y() - offsetY, anchorZ - rotationPoint.z() - offsetZ);
        batch.transform.set(transform);

        final double dx = position.x() - this.context.cameraX();
        final double dy = position.y() - this.context.cameraY();
        final double dz = position.z() - this.context.cameraZ();
        batch.distanceSquared = dx * dx + dy * dy + dz * dz;

        this.batches.add(batch);
        return batch;
    }

    /**
     * Adds a section with uploaded buffers.
     *
     * @param batch         The sub-level batch
     * @param originX       The plot space x origin of the section
     * @param originY       The plot space y origin of the section
     * @param originZ       The plot space z origin of the section
     * @param sortDistance  The distance used to sort translucent geometry, larger is drawn first
     * @param buffersGetter The buffers of the section for each layer, or null for layers without geometry
     */
    public void addSection(final Batch batch, final int originX, final int originY, final int originZ, final double sortDistance, final SectionBuffersGetter buffersGetter) {
        int sectionIndex = -1;

        for (final ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            final SectionBuffers buffers = buffersGetter.get(layer);
            if (buffers == null) {
                continue;
            }

            if (sectionIndex == -1) {
                sectionIndex = this.sectionInfos.size();
                this.sectionInfos.add(new DynamicUniforms.ChunkSectionInfo(
                        new Matrix4f(this.context.frustumMatrix()),
                        originX - batch.anchorX + this.cameraBlockX,
                        originY - batch.anchorY + this.cameraBlockY,
                        originZ - batch.anchorZ + this.cameraBlockZ,
                        1.0F,
                        this.atlasWidth,
                        this.atlasHeight
                ));
            }

            final GpuBuffer indexBuffer;
            final VertexFormat.IndexType indexType;
            if (buffers.getIndexBuffer() == null) {
                this.maxIndicesRequired = Math.max(this.maxIndicesRequired, buffers.getIndexCount());
                indexBuffer = null;
                indexType = null;
            } else {
                indexBuffer = buffers.getIndexBuffer();
                indexType = buffers.getIndexType();
            }

            final int slot = sectionIndex;
            batch.draws.computeIfAbsent(layer, key -> new ArrayList<>()).add(new SortedDraw(sortDistance, new RenderPass.Draw<>(
                    0,
                    buffers.getVertexBuffer(),
                    indexBuffer,
                    indexType,
                    0,
                    buffers.getIndexCount(),
                    (slices, uploader) -> uploader.upload("ChunkSection", slices[slot])
            )));
        }
    }

    /**
     * Uploads the section uniforms. Must be called after all sections are added and before drawing.
     */
    public void upload() {
        this.sectionSlices = RenderSystem.getDynamicUniforms().writeChunkSections(this.sectionInfos.toArray(new DynamicUniforms.ChunkSectionInfo[0]));

        // Translucent geometry is drawn back to front
        this.batches.sort(Comparator.comparingDouble((Batch batch) -> batch.distanceSquared).reversed());
        for (final Batch batch : this.batches) {
            for (final List<SortedDraw> draws : batch.draws.values()) {
                draws.sort(Comparator.comparingDouble(SortedDraw::sortDistance).reversed());
            }
        }
    }

    public boolean isEmpty() {
        return this.sectionInfos.isEmpty();
    }

    public List<Batch> getBatches() {
        return Collections.unmodifiableList(this.batches);
    }

    public int getMaxIndicesRequired() {
        return this.maxIndicesRequired;
    }

    public GpuBufferSlice[] getSectionSlices() {
        return this.sectionSlices;
    }

    @FunctionalInterface
    public interface SectionBuffersGetter {
        @Nullable SectionBuffers get(ChunkSectionLayer layer);
    }

    public record SortedDraw(double sortDistance, RenderPass.Draw<GpuBufferSlice[]> draw) {
    }

    public static final class Batch {
        private final ClientSubLevel subLevel;
        private final int anchorX;
        private final int anchorY;
        private final int anchorZ;
        private final float skyLightScale;
        private final boolean disableFog;
        private final Matrix4f transform = new Matrix4f();
        private final EnumMap<ChunkSectionLayer, List<SortedDraw>> draws = new EnumMap<>(ChunkSectionLayer.class);
        private double distanceSquared;

        private Batch(final ClientSubLevel subLevel, final int anchorX, final int anchorY, final int anchorZ, final float skyLightScale, final boolean disableFog) {
            this.subLevel = subLevel;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.anchorZ = anchorZ;
            this.skyLightScale = skyLightScale;
            this.disableFog = disableFog;
        }

        public ClientSubLevel subLevel() {
            return this.subLevel;
        }

        /**
         * @return The transform from anchor relative plot space into camera relative world space
         */
        public Matrix4fc transform() {
            return this.transform;
        }

        public float skyLightScale() {
            return this.skyLightScale;
        }

        public boolean disableFog() {
            return this.disableFog;
        }

        public List<RenderPass.Draw<GpuBufferSlice[]>> draws(final ChunkSectionLayer layer) {
            final List<SortedDraw> sorted = this.draws.get(layer);
            if (sorted == null) {
                return List.of();
            }
            final List<RenderPass.Draw<GpuBufferSlice[]>> result = new ArrayList<>(sorted.size());
            for (final SortedDraw draw : sorted) {
                result.add(draw.draw());
            }
            return result;
        }
    }
}
