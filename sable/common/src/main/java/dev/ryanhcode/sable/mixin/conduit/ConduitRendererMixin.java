package dev.ryanhcode.sable.mixin.conduit;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.state.CondiutRenderState;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Makes the eye of conduits inside sub-levels face the camera.
 * <p>
 * In 1.21.11 the eye is rotated in {@code submit} by the orientation of the {@link net.minecraft.client.renderer.state.CameraRenderState}
 * instead of a rotation built from the camera yaw & pitch in {@code render}. The pose stack of a block entity inside a
 * sub-level contains the sub-level rotation, so the eye is rotated by the inverse sub-level rotation followed by the
 * world camera rotation.
 */
@Mixin(ConduitRenderer.class)
public class ConduitRendererMixin {

    @ModifyArg(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/CondiutRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 5))
    private Quaternionfc sable$reorientEye(final Quaternionfc cameraOrientation, @Local(argsOnly = true) final CondiutRenderState renderState) {
        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(renderState.blockPos);
        if (subLevel != null) {
            final Quaternionf worldCameraOrientation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
            return subLevel.renderPose().orientation().get(new Quaternionf()).invert().mul(worldCameraOrientation);
        }

        return cameraOrientation;
    }
}
