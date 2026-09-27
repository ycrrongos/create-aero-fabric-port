package com.zurrtum.create.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Stand-in for Create's removed SafeBlockEntityRenderer.
 * Keeps the old renderSafe surface compiling while 1.21.11 uses extract/submit.
 * submit does not yet call into MultiBufferSource; wire that when BER migration lands.
 */
public abstract class SafeBlockEntityRenderer<T extends BlockEntity>
        implements BlockEntityRenderer<T, BlockEntityRenderState> {

    public SafeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    public SafeBlockEntityRenderer() {}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, BlockEntityRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        // Legacy renderSafe path needs a MultiBufferSource bridge; left empty until that lands.
    }

    protected abstract void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay);
}
