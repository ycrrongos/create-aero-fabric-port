package dev.ryanhcode.sable.mixin.respawn_point.sleeping;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinterface.entity.entity_rendering.EntityRenderStateExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Rotates entities sleeping in beds on sub-levels with the sub-level
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void sable$extractSleepingOrientation(final LivingEntity livingEntity, final LivingEntityRenderState renderState, final float partialTick, final CallbackInfo ci) {
        final EntityRenderStateExtension extension = (EntityRenderStateExtension) renderState;
        extension.sable$setSleepingOrientation(null);

        if (livingEntity.getBedOrientation() == null) {
            return;
        }

        final Optional<BlockPos> sleepingPos = livingEntity.getSleepingPos();

        if (sleepingPos.isPresent()) {
            final BlockPos blockPos = sleepingPos.get();

            final SubLevel subLevel = Sable.HELPER.getContaining(livingEntity.level(), blockPos);

            if (subLevel instanceof final ClientSubLevel clientSubLevel) {
                extension.sable$setSleepingOrientation(new Quaternionf(clientSubLevel.renderPose().orientation()));
            }
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;bedOrientation:Lnet/minecraft/core/Direction;", opcode = Opcodes.GETFIELD))
    private void sable$setupRotations(final LivingEntityRenderState renderState, final PoseStack poseStack, final SubmitNodeCollector nodeCollector, final CameraRenderState cameraRenderState, final CallbackInfo ci) {
        final Quaternionf orientation = ((EntityRenderStateExtension) renderState).sable$getSleepingOrientation();

        if (orientation != null) {
            poseStack.mulPose(orientation);
        }
    }

}
