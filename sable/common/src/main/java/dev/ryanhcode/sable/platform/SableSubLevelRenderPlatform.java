package dev.ryanhcode.sable.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.sublevel.render.vanilla.SingleBlockSubLevelWrapper;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public interface SableSubLevelRenderPlatform {
    SableSubLevelRenderPlatform INSTANCE = SablePlatformUtil.load(SableSubLevelRenderPlatform.class);

    /**
     * Tessellates a single block of a single-block sub-level.
     *
     * @param blockAndTintGetter The level wrapper only containing the block
     * @param parts              The model parts of the block
     * @param blockState         The block state
     * @param pos                The plot position of the block
     * @param poseStack          The pose to tessellate with
     * @param vertexConsumer     The consumer of the chunk layer being built
     * @param packedOverlay      The overlay to use
     */
    void tesselateBlock(
            final SingleBlockSubLevelWrapper blockAndTintGetter,
            final List<BlockModelPart> parts,
            final BlockState blockState,
            final BlockPos pos,
            final PoseStack poseStack,
            final VertexConsumer vertexConsumer,
            final int packedOverlay);

    /**
     * @return The chunk layer the block renders into
     */
    ChunkSectionLayer getRenderLayer(
            final SingleBlockSubLevelWrapper blockAndTintGetter,
            final BlockState blockState,
            final BlockPos pos);

    void tryAddFlywheelVisual(final BlockEntity blockEntity);
}
