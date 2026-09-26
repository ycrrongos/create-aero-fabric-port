package dev.ryanhcode.sable.mixin.block_decal_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders block breaking progress of sub-level blocks where the block is in the world.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    // Storage vectors to avoid repeated allocation
    private final @Unique Quaternionf sable$orientationStorage = new Quaternionf();

    @Shadow
    @Nullable
    private ClientLevel level;

    /**
     * Sub-level blocks are culled by their distance in the world, instead of their distance in the plot
     */
    @WrapOperation(method = "extractBlockDestroyAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distToCenterSqr(DDD)D"))
    private double sable$blockDamageDistance(final BlockPos pos, final double x, final double y, final double z, final Operation<Double> original) {
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(this.level, pos);
        if (subLevel == null) {
            return original.call(pos, x, y, z);
        }

        return subLevel.renderPose().transformPosition(pos.getCenter()).distanceToSqr(x, y, z);
    }

    @Inject(method = "renderBlockDestroyAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;", shift = At.Shift.BEFORE))
    private void sable$preRenderBlockDamage(final PoseStack poseStack, final net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource, final LevelRenderState renderState, final CallbackInfo ci,
                                            @Local final BlockPos pos) {
        final Vec3 plotPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(this.level, plotPos);

        if (subLevel == null) {
            return;
        }

        final Pose3dc renderPose = subLevel.renderPose();
        final Vec3 cameraPos = renderState.cameraRenderState.pos;
        final Vec3 projectedPos = renderPose.transformPosition(plotPos);

        poseStack.popPose();
        poseStack.pushPose();

        poseStack.translate(projectedPos.x - cameraPos.x, projectedPos.y - cameraPos.y, projectedPos.z - cameraPos.z);
        poseStack.mulPose(this.sable$orientationStorage.set(renderPose.orientation()));
    }
}
