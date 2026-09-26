package dev.ryanhcode.sable.mixin.camera.camera_zoom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.SableClientConfig;
import dev.ryanhcode.sable.mixinhelpers.camera.new_camera_types.SableCameraTypes;
import dev.ryanhcode.sable.mixinterface.camera.camera_zoom.CameraZoomExtension;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow @Final private Minecraft minecraft;

    /**
     * In 1.21.11 {@code Inventory#swapPaint} was replaced with {@code Inventory#setSelectedSlot} and
     * {@code ScrollWheelHandler#getNextScrollWheelSelection}. The scroll amount that used to be passed to
     * {@code swapPaint} is the {@code int} local passed to {@code getNextScrollWheelSelection}.
     */
    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    private void sable$onScroll(final Inventory instance, final int slot, final Operation<Void> original, @Local(ordinal = 0) final int scroll) {
        final double d = scroll;
        final CameraType cameraType = this.minecraft.options.getCameraType();
        if (cameraType == SableCameraTypes.SUB_LEVEL_VIEW || cameraType == SableCameraTypes.SUB_LEVEL_VIEW_UNLOCKED) {
            final CameraZoomExtension extension = ((CameraZoomExtension) this.minecraft.gameRenderer.getMainCamera());

            extension.sable$setZoomAmount((float) (extension.sable$getZoomAmount() - d * SableClientConfig.ZOOM_SENSITIVITY.get()));
            return;
        }

        original.call(instance, slot);
    }
}