package dev.ryanhcode.sable.mixin.entity.entity_rotations_and_riding;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.mixinterface.entity.entity_rendering.EntityRenderStateExtension;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Rotates entity models (and their fire) by the orientation they inherit from sub-levels they stand on or ride.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Unique
    private static boolean sable$pushModelOrientation(final EntityRenderState renderState, final PoseStack poseStack) {
        final EntityRenderStateExtension extension = (EntityRenderStateExtension) renderState;
        final Quaternionf orientation = extension.sable$getModelOrientation();
        if (orientation == null) {
            return false;
        }

        final Vector3f offset = extension.sable$getModelOffset();
        final Vector3f pivot = extension.sable$getModelPivot();

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.rotateAround(orientation, pivot.x, pivot.y, pivot.z);
        return true;
    }

    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"))
    private <S extends EntityRenderState> void sable$rotateEntity(final EntityRenderer<?, S> renderer, final S renderState, final PoseStack poseStack, final SubmitNodeCollector nodeCollector,
                                                                  final CameraRenderState cameraRenderState, final Operation<Void> original) {
        final boolean rotated = sable$pushModelOrientation(renderState, poseStack);
        try {
            original.call(renderer, renderState, poseStack, nodeCollector, cameraRenderState);
        } finally {
            if (rotated) {
                poseStack.popPose();
            }
        }
    }

    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitFlame(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V"))
    private void sable$rotateFlame(final SubmitNodeCollector instance, final PoseStack poseStack, final EntityRenderState renderState, final Quaternionf rotation, final Operation<Void> original) {
        final boolean rotated = sable$pushModelOrientation(renderState, poseStack);
        try {
            original.call(instance, poseStack, renderState, rotation);
        } finally {
            if (rotated) {
                poseStack.popPose();
            }
        }
    }
}
