package dev.ryanhcode.sable.mixin.entity.entity_rotations_and_riding;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper;
import dev.ryanhcode.sable.mixinterface.entity.entity_rendering.EntityRenderStateExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entities riding or standing on rotated sub-levels are rendered with the orientation they inherit from them.
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void sable$extractOrientation(final Entity entity, final EntityRenderState state, final float partialTick, final CallbackInfo ci) {
        final EntityRenderStateExtension extension = (EntityRenderStateExtension) state;

        if (!EntitySubLevelUtil.shouldKick(entity)) {
            extension.sable$setModelOrientation(null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            return;
        }

        final Quaterniond orientation = EntitySubLevelRotationHelper.getEntityOrientation(entity, x -> ((ClientSubLevel) x).renderPose(), partialTick, EntitySubLevelRotationHelper.Type.ENTITY);
        if (orientation == null) {
            extension.sable$setModelOrientation(null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            return;
        }

        final Vec3 eyeOffset = entity.getEyePosition().subtract(entity.position());
        final Vec3 offset = Sable.HELPER.getEyePositionInterpolated(entity, partialTick).subtract(entity.getEyePosition(partialTick));
        extension.sable$setModelOrientation(new Quaternionf(orientation), offset.x, offset.y, offset.z, eyeOffset.x, eyeOffset.y, eyeOffset.z);
    }

    /**
     * The model is rotated, so the name tag has to be counter-rotated to still face the camera
     */
    @WrapOperation(method = "submitNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V"))
    private void sable$submitNameTag(final SubmitNodeCollector instance, final PoseStack poseStack, final Vec3 pos, final int yOffset, final Component text, final boolean seeThrough,
                                     final int packedLight, final double distanceToCameraSq, final CameraRenderState cameraRenderState, final Operation<Void> original,
                                     @Local(argsOnly = true) final EntityRenderState renderState) {
        final Quaternionf orientation = ((EntityRenderStateExtension) renderState).sable$getModelOrientation();
        if (orientation == null) {
            original.call(instance, poseStack, pos, yOffset, text, seeThrough, packedLight, distanceToCameraSq, cameraRenderState);
            return;
        }

        final Quaternionf cameraOrientation = cameraRenderState.orientation;
        cameraRenderState.orientation = new Quaternionf(orientation).conjugate().mul(cameraOrientation);
        try {
            original.call(instance, poseStack, pos, yOffset, text, seeThrough, packedLight, distanceToCameraSq, cameraRenderState);
        } finally {
            cameraRenderState.orientation = cameraOrientation;
        }
    }
}
