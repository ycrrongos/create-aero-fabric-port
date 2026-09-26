package dev.ryanhcode.sable.fabric.mixin.block_outline_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinhelpers.block_outline_render.SubLevelCamera;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Transforms block hover outlines for sublevels.
 * <p>
 * The outline is drawn with the pose stack moved into the local space of the sub-level, and the camera render state
 * moved to the sub-level local camera position, so vanilla outlines and Fabric's {@code BEFORE_BLOCK_OUTLINE} listeners
 * both render in plot space.
 */
@Mixin(value = LevelRenderer.class, priority = 400)
public abstract class LevelRendererMixin {

    // Storage vectors to avoid repeated allocation
    private final @Unique Quaternionf sable$orientationStorage = new Quaternionf();
    private final @Unique SubLevelCamera sable$sublevelCamera = new SubLevelCamera();

    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderBlockOutline(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;ZLnet/minecraft/client/renderer/state/LevelRenderState;)V"))
    private void sable$renderSubLevelBlockOutline(final LevelRenderer instance, final MultiBufferSource.BufferSource bufferSource, final PoseStack poseStack, final boolean translucent, final LevelRenderState renderState, final Operation<Void> original) {
        final BlockOutlineRenderState outline = renderState.blockOutlineRenderState;
        final ClientSubLevel subLevel = outline != null && this.level != null ? (ClientSubLevel) Sable.HELPER.getContaining(this.level, outline.pos()) : null;

        if (subLevel == null) {
            original.call(instance, bufferSource, poseStack, translucent, renderState);
            return;
        }

        final CameraRenderState cameraState = renderState.cameraRenderState;
        final Vec3 realCameraPosition = cameraState.pos;
        final Pose3dc pose = subLevel.renderPose();

        this.sable$sublevelCamera.setCamera(this.minecraft.gameRenderer.getMainCamera());
        this.sable$sublevelCamera.setPose(pose);
        final Vec3 cameraPosition = this.sable$sublevelCamera.position();

        final Vector3dc position = pose.position();
        final Vector3dc rotationPoint = pose.rotationPoint();
        final Quaterniondc orientation = pose.orientation();
        final Vector3dc scale = pose.scale();

        poseStack.pushPose();
        poseStack.translate(
                (float) (position.x() - realCameraPosition.x),
                (float) (position.y() - realCameraPosition.y),
                (float) (position.z() - realCameraPosition.z)
        );
        poseStack.mulPose(this.sable$orientationStorage.set(orientation));
        poseStack.translate(
                (float) -(rotationPoint.x() - cameraPosition.x),
                (float) -(rotationPoint.y() - cameraPosition.y),
                (float) -(rotationPoint.z() - cameraPosition.z)
        );
        poseStack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());

        cameraState.pos = cameraPosition;
        try {
            original.call(instance, bufferSource, poseStack, translucent, renderState);
        } finally {
            cameraState.pos = realCameraPosition;
            poseStack.popPose();
            this.sable$sublevelCamera.clear();
        }
    }
}
