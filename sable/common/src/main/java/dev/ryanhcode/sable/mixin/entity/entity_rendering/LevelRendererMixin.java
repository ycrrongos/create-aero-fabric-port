package dev.ryanhcode.sable.mixin.entity.entity_rendering;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.entity.entity_rendering.EntityRenderStateExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Renders entities inside sub-levels (and entities standing on them) at their position in the world.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyReturnValue(method = "extractEntity", at = @At("RETURN"))
    private EntityRenderState sable$extractEntityOnSubLevel(final EntityRenderState state, final Entity entity, final float partialTick) {
        final EntityRenderStateExtension extension = (EntityRenderStateExtension) state;
        extension.sable$setSubLevelOrientation(null);

        // Render the entity on the data
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(entity);

        if (subLevel == null) {
            // Tracking sub-levels
            final SubLevel trackingSubLevel = Sable.HELPER.getTrackingSubLevel(entity);

            if (trackingSubLevel instanceof final ClientSubLevel clientSubLevel && !entity.isPassenger()) {
                final Vector3d oldTrackingPosLocal = trackingSubLevel.lastPose().transformPositionInverse(new Vector3d(entity.xOld, entity.yOld, entity.zOld));
                final Vector3d newTrackingPosLocal = trackingSubLevel.logicalPose().transformPositionInverse(JOMLConversion.toJOML(entity.position()));

                final Vector3d interpolatedTrackingPosLocal = new Vector3d(
                        Mth.lerp(partialTick, oldTrackingPosLocal.x, newTrackingPosLocal.x),
                        Mth.lerp(partialTick, oldTrackingPosLocal.y, newTrackingPosLocal.y),
                        Mth.lerp(partialTick, oldTrackingPosLocal.z, newTrackingPosLocal.z)
                );

                final Pose3dc renderPose = clientSubLevel.renderPose(partialTick);
                renderPose.transformPosition(interpolatedTrackingPosLocal);

                state.x = interpolatedTrackingPosLocal.x;
                state.y = interpolatedTrackingPosLocal.y;
                state.z = interpolatedTrackingPosLocal.z;
            }

            return state;
        }

        final Pose3dc renderPose = subLevel.renderPose(partialTick);
        final Vector3d transformedPosition = renderPose.transformPosition(new Vector3d(state.x, state.y, state.z));

        extension.sable$setSubLevelOrientation(new Quaternionf(renderPose.orientation()));

        state.x = transformedPosition.x;
        state.y = transformedPosition.y;
        state.z = transformedPosition.z;
        return state;
    }

    @WrapOperation(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private void sable$submitEntityOnSubLevel(final EntityRenderDispatcher instance,
                                              final EntityRenderState renderState,
                                              final CameraRenderState cameraRenderState,
                                              final double x,
                                              final double y,
                                              final double z,
                                              final PoseStack poseStack,
                                              final SubmitNodeCollector nodeCollector,
                                              final Operation<Void> original) {
        final Quaternionf orientation = ((EntityRenderStateExtension) renderState).sable$getSubLevelOrientation();
        if (orientation != null) {
            poseStack.pushPose();
            poseStack.rotateAround(orientation, (float) x, (float) y, (float) z);
            original.call(instance, renderState, cameraRenderState, x, y, z, poseStack, nodeCollector);
            poseStack.popPose();
        } else {
            original.call(instance, renderState, cameraRenderState, x, y, z, poseStack, nodeCollector);
        }
    }
}
