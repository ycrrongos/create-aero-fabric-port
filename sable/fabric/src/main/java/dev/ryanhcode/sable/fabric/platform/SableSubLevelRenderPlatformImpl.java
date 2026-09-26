package dev.ryanhcode.sable.fabric.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.platform.SableSubLevelRenderPlatform;
import dev.ryanhcode.sable.sublevel.render.vanilla.SingleBlockSubLevelWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class SableSubLevelRenderPlatformImpl implements SableSubLevelRenderPlatform {

    @Override
    public void tesselateBlock(final SingleBlockSubLevelWrapper blockAndTintGetter, final List<BlockModelPart> parts, final BlockState blockState, final BlockPos pos, final PoseStack poseStack, final VertexConsumer vertexConsumer, final int packedOverlay) {
        Minecraft.getInstance().getBlockRenderer().modelRenderer.tesselateWithoutAO(blockAndTintGetter, parts, blockState, pos, poseStack, vertexConsumer, true, packedOverlay);
    }

    @Override
    public ChunkSectionLayer getRenderLayer(final SingleBlockSubLevelWrapper blockAndTintGetter, final BlockState blockState, final BlockPos pos) {
        return ItemBlockRenderTypes.getChunkRenderType(blockState);
    }

    @Override
    public void tryAddFlywheelVisual(final BlockEntity blockEntity) {
        SableFlywheelVisuals.tryAddVisual(blockEntity);
    }
}
