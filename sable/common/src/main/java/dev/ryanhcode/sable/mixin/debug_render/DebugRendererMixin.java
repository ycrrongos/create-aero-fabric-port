package dev.ryanhcode.sable.mixin.debug_render;

import dev.ryanhcode.sable.SableClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Emits the gizmos of the Sable sub-level gizmo tool alongside the vanilla debug gizmos.
 */
@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(method = "emitGizmos", at = @At("TAIL"))
    private void sable$emitGizmos(final Frustum frustum, final double camX, final double camY, final double camZ, final float partialTick, final CallbackInfo ci) {
        SableClient.GIZMO_HANDLER.emitGizmos(partialTick);
    }
}
