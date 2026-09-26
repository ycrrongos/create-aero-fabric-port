package foundry.veil.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import foundry.veil.impl.client.render.VeilProjectionTracker;
import net.minecraft.client.renderer.CachedPerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CachedPerspectiveProjectionMatrixBuffer.class)
public abstract class CachedPerspectiveProjectionMatrixBufferMixin {

    @Shadow
    protected abstract Matrix4f createProjectionMatrix(int width, int height, float fov);

    @ModifyReturnValue(method = "getBuffer", at = @At("RETURN"))
    private GpuBufferSlice veil$record(GpuBufferSlice slice, int width, int height, float fov) {
        VeilProjectionTracker.record(slice, this.createProjectionMatrix(width, height, fov));
        return slice;
    }
}
