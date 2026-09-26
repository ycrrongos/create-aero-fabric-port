package foundry.veil.mixin.client;

import foundry.veil.impl.client.render.VeilFogState;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(method = "updateBuffer", at = @At("HEAD"))
    private void veil$captureFog(ByteBuffer buffer, int position, Vector4f fogColor, float environmentalStart, float environmentalEnd, float renderDistanceStart, float renderDistanceEnd, float skyEnd, float cloudEnd, CallbackInfo ci) {
        VeilFogState.set(new VeilFogState(fogColor.x(), fogColor.y(), fogColor.z(), fogColor.w(), environmentalStart, environmentalEnd, renderDistanceStart, renderDistanceEnd, skyEnd, cloudEnd));
    }
}
