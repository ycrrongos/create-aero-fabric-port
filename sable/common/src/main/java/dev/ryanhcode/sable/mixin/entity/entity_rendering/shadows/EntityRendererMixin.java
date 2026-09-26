package dev.ryanhcode.sable.mixin.entity.entity_rendering.shadows;

import dev.ryanhcode.sable.mixinhelpers.entity.entity_rendering.shadows.SubLevelEntityShadowRenderer;
import dev.ryanhcode.sable.mixinterface.entity.entity_rendering.EntityRenderStateExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Computes the shadows entities cast onto sub-levels when their render state is created.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Shadow
    protected abstract float getShadowStrength(EntityRenderState renderState);

    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    private void sable$extractShadowsOnSubLevels(final Entity entity, final float partialTick, final CallbackInfoReturnable<EntityRenderState> cir) {
        final EntityRenderState state = cir.getReturnValue();
        final EntityRenderStateExtension extension = (EntityRenderStateExtension) state;
        extension.sable$setSubLevelShadow(null);

        if (!Minecraft.getInstance().options.entityShadows().get() || state.isInvisible || state.shadowRadius <= 0.0F) {
            return;
        }

        // Same strength as the vanilla shadow of the entity
        final float strength = (float) ((1.0 - state.distanceToCameraSq / 256.0) * this.getShadowStrength(state));
        if (strength <= 0.0F) {
            return;
        }

        extension.sable$setSubLevelShadow(SubLevelEntityShadowRenderer.computeEntityShadowOnSubLevels(entity, strength, partialTick, state.shadowRadius));
    }
}
