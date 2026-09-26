package foundry.veil.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import foundry.veil.impl.client.render.VeilProjectionTracker;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PerspectiveProjectionMatrixBuffer.class)
public class ProjectionMatrixBufferMixin {

    @Inject(method = "getBuffer", at = @At("RETURN"))
    private void veil$record(Matrix4f pose, CallbackInfoReturnable<GpuBufferSlice> cir) {
        VeilProjectionTracker.record(cir.getReturnValue(), pose);
    }
}
