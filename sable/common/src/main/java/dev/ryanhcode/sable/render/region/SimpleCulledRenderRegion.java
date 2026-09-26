package dev.ryanhcode.sable.render.region;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * A static mesh of the outer faces of a set of blocks, following the sub-level it is part of.
 * <p>
 * Two copies of the mesh are uploaded: one with the regular winding and one with the winding reversed, so both the
 * closest front faces and the closest back faces can be drawn with back-face culling.
 */
public abstract class SimpleCulledRenderRegion {
    private Collection<BlockPos> unbuiltData;
    private boolean built = false;
    private GpuBuffer frontBuffer;
    private GpuBuffer backBuffer;
    private int indexCount;
    private Vec3 origin;

    public SimpleCulledRenderRegion(final Collection<BlockPos> blocks) {
        this.unbuiltData = blocks;
    }

    /**
     * Draws this region.
     *
     * @param renderPass    The render pass to draw with, set up with a pipeline using the vertex format of this region
     * @param frustumMatrix The view rotation matrix
     * @param camera        The camera position
     * @param backFaces     Whether the back faces should be drawn instead of the front faces
     */
    public void render(final RenderPass renderPass, final Matrix4fc frustumMatrix, final Vec3 camera, final boolean backFaces) {
        if (!this.built) {
            this.build();
        }

        if (this.indexCount == 0) {
            return;
        }

        final Minecraft client = Minecraft.getInstance();
        final SubLevel subLevel = Sable.HELPER.getContaining(client.level, this.origin);

        Vec3 globalOrigin = this.origin;
        final Quaternionf globalOrientation = new Quaternionf();

        if (subLevel instanceof final ClientSubLevel clientSubLevel) {
            final Pose3dc renderPose = clientSubLevel.renderPose();
            globalOrigin = renderPose.transformPosition(globalOrigin);
            globalOrientation.set(renderPose.orientation());
        }

        final Vec3 relativePos = globalOrigin.subtract(camera);

        final Matrix4f modelViewMatrix = new Matrix4f(frustumMatrix)
                .translate((float) relativePos.x, (float) relativePos.y, (float) relativePos.z)
                .rotate(globalOrientation);

        final GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(modelViewMatrix, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

        final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        renderPass.setUniform("DynamicTransforms", transforms);
        renderPass.setVertexBuffer(0, backFaces ? this.backBuffer : this.frontBuffer);
        renderPass.setIndexBuffer(indices.getBuffer(this.indexCount), indices.type());
        renderPass.drawIndexed(0, 0, this.indexCount, 1);
    }

    public void build() {
        final BlockPos firstBlock = this.unbuiltData.stream().findFirst().orElseThrow();
        final Vector3i minBlock = new Vector3i(firstBlock.getX(), firstBlock.getY(), firstBlock.getZ());
        final Vector3i maxBlock = new Vector3i(firstBlock.getX(), firstBlock.getY(), firstBlock.getZ());
        final Vector3i currentBlock = new Vector3i();

        for (final BlockPos block : this.unbuiltData) {
            currentBlock.set(block.getX(), block.getY(), block.getZ());
            minBlock.min(currentBlock);
            maxBlock.max(currentBlock);
        }

        int gridSize = maxBlock.x() - minBlock.x() + 1;
        gridSize = Math.max(gridSize, maxBlock.y() - minBlock.y() + 1);
        gridSize = Math.max(gridSize, maxBlock.z() - minBlock.z() + 1);

        final BlockPos originBlock = new BlockPos(minBlock.x(), minBlock.y(), minBlock.z());
        this.origin = Vec3.atLowerCornerOf(originBlock);

        final SimpleCulledRenderRegionBuilder builder = this.createMeshBuilder(gridSize);

        for (final BlockPos blockPos : this.unbuiltData) {
            builder.add(blockPos.getX() - originBlock.getX(), blockPos.getY() - originBlock.getY(), blockPos.getZ() - originBlock.getZ());
        }

        builder.buildNoGreedy();

        try (final ByteBufferBuilder byteBuffer = new ByteBufferBuilder(this.getVertexFormat().getVertexSize() * 4 * 256)) {
            final BufferBuilder bufferBuilder = new BufferBuilder(byteBuffer, VertexFormat.Mode.QUADS, this.getVertexFormat());
            builder.render(new Matrix4f(), bufferBuilder);

            try (final MeshData meshData = bufferBuilder.build()) {
                this.unbuiltData = null;
                this.built = true;

                if (meshData == null) {
                    this.indexCount = 0;
                    return;
                }

                final ByteBuffer vertices = meshData.vertexBuffer();
                this.indexCount = meshData.drawState().indexCount();
                this.frontBuffer = RenderSystem.getDevice().createBuffer(() -> "Sable culled region", GpuBuffer.USAGE_VERTEX, vertices);
                this.backBuffer = RenderSystem.getDevice().createBuffer(() -> "Sable culled region (reversed)", GpuBuffer.USAGE_VERTEX,
                        reverseQuadWinding(vertices, this.getVertexFormat().getVertexSize()));
            }
        }
    }

    /**
     * Copies quad vertex data with the winding of every quad reversed.
     */
    private static ByteBuffer reverseQuadWinding(final ByteBuffer vertices, final int vertexSize) {
        final int start = vertices.position();
        final int length = vertices.remaining();
        final ByteBuffer reversed = ByteBuffer.allocateDirect(length).order(vertices.order());
        final int quadSize = vertexSize * 4;
        final byte[] vertex = new byte[vertexSize];

        for (int quad = 0; quad < length; quad += quadSize) {
            // 0 1 2 3 -> 0 3 2 1
            for (final int index : new int[]{0, 3, 2, 1}) {
                vertices.get(start + quad + index * vertexSize, vertex);
                reversed.put(vertex);
            }
        }

        reversed.flip();
        return reversed;
    }

    public Vec3 getOrigin() {
        return this.origin;
    }

    public abstract SimpleCulledRenderRegionBuilder createMeshBuilder(int gridSize);

    public abstract VertexFormat getVertexFormat();

    public void free() {
        if (this.built && this.frontBuffer != null) {
            this.frontBuffer.close();
            this.backBuffer.close();
            this.frontBuffer = null;
            this.backBuffer = null;
        }
    }
}
