package dev.ryanhcode.sable.mixin.entity.entity_rendering;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The camera distance of entities inside sub-levels (used for name tags and shadows) is measured at their position in
 * the world instead of their position in the plot.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Shadow
    public Camera camera;

    @Inject(method = "distanceToSqr(Lnet/minecraft/world/entity/Entity;)D", at = @At("HEAD"), cancellable = true)
    private void sable$distanceInWorld(final Entity entity, final CallbackInfoReturnable<Double> cir) {
        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(entity);
        if (subLevel != null && this.camera != null) {
            cir.setReturnValue(this.camera.position().distanceToSqr(subLevel.renderPose().transformPosition(entity.position())));
        }
    }
}
