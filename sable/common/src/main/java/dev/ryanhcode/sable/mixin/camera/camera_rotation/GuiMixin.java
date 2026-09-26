package dev.ryanhcode.sable.mixin.camera.camera_rotation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fix f3 crosshair when riding entity in sub-level
 * <p>
 * In 1.21.11 the 3D debug crosshair is no longer drawn by {@code Gui#renderCrosshair}, but in world space by
 * {@link DebugScreenOverlay#render3dCrosshair(Camera)} after the level is rendered. The crosshair is rotated by the
 * inverse camera rotation there, so the rotation of the sub-level the camera entity rides in is taken into account.
 */
@Mixin(DebugScreenOverlay.class)
public class GuiMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render3dCrosshair", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewStack()Lorg/joml/Matrix4fStack;"))
    private void sable$onRenderCrosshair(final Camera crosshairCamera, final CallbackInfo ci, @Share("mountedOrientation") final LocalRef<Quaterniond> mountedOrientation) {
        final Camera camera = this.minecraft.gameRenderer.getMainCamera();
        final Entity entity = camera.entity();

        final float pt = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        final Quaterniond ridingOrientation = EntitySubLevelRotationHelper.getEntityOrientation(entity, (x) -> ((ClientSubLevel) x).renderPose(), pt, EntitySubLevelRotationHelper.Type.CAMERA);
        mountedOrientation.set(ridingOrientation);
    }

    @Redirect(method = "render3dCrosshair", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;rotateX(F)Lorg/joml/Matrix4f;"))
    private Matrix4f sable$redirectRotateX(final Matrix4fStack stack, final float angle, @Share("mountedOrientation") final LocalRef<Quaterniond> mountedOrientation) {
        if (mountedOrientation.get() != null) {
            final float pt = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
            final Camera camera = this.minecraft.gameRenderer.getMainCamera();
            final Entity entity = camera.entity();

            return stack.rotateX(entity.getViewXRot(pt) * (float) (Math.PI / 180.0));
        }

        return stack.rotateX(angle);
    }

    @Redirect(method = "render3dCrosshair", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;rotateY(F)Lorg/joml/Matrix4f;"))
    private Matrix4f sable$redirectRotateY(final Matrix4fStack stack, final float angle, @Share("mountedOrientation") final LocalRef<Quaterniond> mountedOrientation) {
        if (mountedOrientation.get() != null) {
            final float pt = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
            final Camera camera = this.minecraft.gameRenderer.getMainCamera();
            final Entity entity = camera.entity();

            return stack.rotateY(entity.getViewYRot(pt) * (float) (Math.PI / 180.0));
        }

        return stack.rotateY(angle);
    }

    /**
     * The {@code scale(-f, f, -f)} following the rotations is a uniform scale combined with a half turn around the Y axis,
     * which is part of the inverse camera rotation in 1.21.11. The inverse riding orientation has to be applied after it.
     */
    @WrapOperation(method = "render3dCrosshair", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;scale(FFF)Lorg/joml/Matrix4f;"))
    private Matrix4f sable$rotateAfterScale(final Matrix4fStack stack, final float x, final float y, final float z, final Operation<Matrix4f> original, @Share("mountedOrientation") final LocalRef<Quaterniond> mountedOrientation) {
        final Matrix4f result = original.call(stack, x, y, z);

        if (mountedOrientation.get() != null) {
            return stack.rotate(new Quaternionf(mountedOrientation.get()).conjugate());
        }

        return result;
    }

}
