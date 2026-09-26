package foundry.veil.mixin.client;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.impl.client.render.VeilProjectionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(method = "setProjectionMatrix", at = @At("TAIL"))
    private static void veil$setProjection(GpuBufferSlice slice, ProjectionType type, CallbackInfo ci) {
        VeilProjectionTracker.onSetProjection(slice);
    }

    @Inject(method = "backupProjectionMatrix", at = @At("TAIL"))
    private static void veil$backup(CallbackInfo ci) {
        VeilProjectionTracker.onBackup();
    }

    @Inject(method = "restoreProjectionMatrix", at = @At("TAIL"))
    private static void veil$restore(CallbackInfo ci) {
        VeilProjectionTracker.onRestore();
    }
}
