package dev.ryanhcode.sable.sublevel.render.vanilla;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.dynamic_directional_shading.ModelBlockRendererCacheExtension;
import dev.ryanhcode.sable.platform.SableSubLevelRenderPlatform;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderContext;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.SubLevelSectionDraws;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A renderer for a {@link dev.ryanhcode.sable.sublevel.SubLevel} consisting of a single block.
 * <p>
 * The block is tessellated every frame, lit with the light at the position of the sub-level in the world, and drawn
 * with the vanilla terrain pipeline of its chunk layer.
 */
public class VanillaSingleSubLevelRenderData implements SubLevelRenderData {

    private static final SingleBlockSubLevelWrapper LEVEL_WRAPPER = new SingleBlockSubLevelWrapper();
    private static final int INITIAL_BUFFER_SIZE = 16 * 1024;

    /**
     * The sub-level this renderer is for
     */
    private final ClientSubLevel subLevel;

    /**
     * The cached block state for single block rendering
     */
    private BlockState singleBlockState = null;

    /**
     * The cached block position for single block rendering
     */
    private BlockPos singleBlockPos = null;

    /**
     * The cached block seed for single block rendering
     */
    private long singleBlockSeed = 42L;

    /**
     * The cached block entity for single block rendering, if it isn't rendered globally
     */
    private @Nullable BlockEntity singleBlockEntity = null;

    private final RandomSource random = RandomSource.create();
    private final List<BlockModelPart> parts = new ArrayList<>();
    private @Nullable ByteBufferBuilder byteBuffer;
    private @Nullable GpuBuffer vertexBuffer;
    private @Nullable SectionBuffers buffers;
    private @Nullable ChunkSectionLayer bufferLayer;

    /**
     * Creates a new renderer for the given sub-level
     *
     * @param subLevel the sub-level to render
     */
    public VanillaSingleSubLevelRenderData(final ClientSubLevel subLevel) {
        this.subLevel = subLevel;
        this.rebuild();
    }

    private void handleBlockEntity(@Nullable final BlockEntity blockEntity) {
        if (blockEntity == null) {
            this.singleBlockEntity = null;
            return;
        }

        final BlockEntityRenderer<BlockEntity, ?> blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);

        // Block entities rendered off screen are rendered through the globally rendered block entities of the level
        this.singleBlockEntity = blockEntityRenderer != null && !blockEntityRenderer.shouldRenderOffScreen() ? blockEntity : null;
    }

    /**
     * Tessellates the block into the vertex buffer of its chunk layer.
     *
     * @return Whether anything was tessellated
     */
    private boolean tessellate(final Pose3dc renderPose) {
        final Minecraft client = Minecraft.getInstance();
        if (this.singleBlockState.isAir()) {
            this.rebuild();
        }

        if (this.singleBlockState.getRenderShape() != RenderShape.MODEL) {
            return false;
        }

        final Vec3 renderPos = renderPose.transformPosition(Vec3.atCenterOf(this.singleBlockPos));
        LEVEL_WRAPPER.setup(this.subLevel.getLevel(), renderPos.x, renderPos.y, renderPos.z, this.singleBlockPos, this.singleBlockState);

        try {
            final ChunkSectionLayer layer = SableSubLevelRenderPlatform.INSTANCE.getRenderLayer(LEVEL_WRAPPER, this.singleBlockState, this.singleBlockPos);

            this.random.setSeed(this.singleBlockSeed);
            this.parts.clear();
            client.getBlockRenderer().getBlockModel(this.singleBlockState).collectParts(this.random, this.parts);

            if (this.byteBuffer == null) {
                this.byteBuffer = new ByteBufferBuilder(INITIAL_BUFFER_SIZE);
            }

            final BufferBuilder builder = new BufferBuilder(this.byteBuffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            final ModelBlockRendererCacheExtension cache = (ModelBlockRendererCacheExtension) ModelBlockRenderer.CACHE.get();
            cache.sable$setOnSubLevel(true);
            try {
                SableSubLevelRenderPlatform.INSTANCE.tesselateBlock(LEVEL_WRAPPER, this.parts, this.singleBlockState, this.singleBlockPos, new PoseStack(), builder, OverlayTexture.NO_OVERLAY);
            } finally {
                cache.sable$setOnSubLevel(false);
            }

            try (final MeshData meshData = builder.build()) {
                if (meshData == null) {
                    return false;
                }

                this.upload(layer, meshData);
                return true;
            }
        } finally {
            LEVEL_WRAPPER.clear();
        }
    }

    private void upload(final ChunkSectionLayer layer, final MeshData meshData) {
        final ByteBuffer vertices = meshData.vertexBuffer();
        if (this.vertexBuffer == null || this.vertexBuffer.isClosed() || this.vertexBuffer.size() < vertices.remaining()) {
            if (this.vertexBuffer != null) {
                this.vertexBuffer.close();
            }
            this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Sable single block sub-level vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, vertices);
        } else {
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.vertexBuffer.slice(0, vertices.remaining()), vertices);
        }

        final MeshData.DrawState drawState = meshData.drawState();
        this.buffers = new SectionBuffers(this.vertexBuffer, null, drawState.indexCount(), drawState.indexType());
        this.bufferLayer = layer;
    }

    @Override
    public void collectDraws(final SubLevelSectionDraws draws) {
        final SubLevelRenderContext context = draws.getContext();
        final Pose3dc renderPose = this.subLevel.renderPose(context.partialTicks());

        if (!this.tessellate(renderPose) || this.buffers == null) {
            return;
        }

        final BlockPos pos = this.singleBlockPos;
        final SubLevelSectionDraws.Batch batch = draws.begin(this.subLevel, renderPose, pos.getX(), pos.getY(), pos.getZ(), 1.0F, false);
        final ChunkSectionLayer layer = this.bufferLayer;
        final SectionBuffers sectionBuffers = this.buffers;
        draws.addSection(batch, pos.getX(), pos.getY(), pos.getZ(), 0.0, requested -> requested == layer ? sectionBuffers : null);
    }

    public @Nullable BlockEntity getRenderBlockEntity() {
        if (this.singleBlockState.isAir()) {
            this.rebuild();
        }
        return this.singleBlockEntity;
    }

    @Override
    public void rebuild() {
        final BoundingBox3ic bounds = this.subLevel.getPlot().getBoundingBox();
        final BlockPos pos = new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ());

        final BlockState blockState = this.subLevel.getLevel().getBlockState(pos);

        this.singleBlockState = blockState;
        this.singleBlockPos = pos;
        this.singleBlockSeed = blockState.getSeed(pos);

        this.handleBlockEntity(blockState.hasBlockEntity() ? this.subLevel.getLevel().getBlockEntity(pos) : null);

        if (this.singleBlockEntity != null) {
            SableSubLevelRenderPlatform.INSTANCE.tryAddFlywheelVisual(this.singleBlockEntity);
        }
    }

    @Override
    public void compileSections(final PrioritizeChunkUpdates chunkUpdates, final RenderRegionCache renderRegionCache, final Camera camera) {
    }

    @Override
    public int getVisibleSectionCount() {
        return 1;
    }

    @Override
    public ClientSubLevel getSubLevel() {
        return this.subLevel;
    }

    @Override
    public void setDirty(final int x, final int y, final int z, final boolean playerChanged) {
        this.rebuild();
    }

    @Override
    public boolean isSectionCompiled(final int x, final int y, final int z) {
        return true;
    }

    @Override
    public void close() {
        this.singleBlockEntity = null;
        this.buffers = null;
        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
            this.vertexBuffer = null;
        }
        if (this.byteBuffer != null) {
            this.byteBuffer.close();
            this.byteBuffer = null;
        }
    }
}
