package foundry.veil.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import foundry.veil.impl.client.render.VeilProjectionTracker;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CachedOrthoProjectionMatrixBuffer.class)
public abstract class CachedOrthoProjectionMatrixBufferMixin {

    @Shadow
    protected abstract Matrix4f createProjectionMatrix(float width, float height);

    @ModifyReturnValue(method = "getBuffer", at = @At("RETURN"))
    private GpuBufferSlice veil$record(GpuBufferSlice slice, float width, float height) {
        VeilProjectionTracker.record(slice, this.createProjectionMatrix(width, height));
        return slice;
    }
}
