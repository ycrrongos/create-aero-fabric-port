package dev.ryanhcode.sable.fabric.mixin.compatibility.create.behaviour_compatibility.harvester_block_entity;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterRenderer;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.harvester.HarvesterLerpedSpeed;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create Fly computes the blade transform of harvesters while extracting the render state
 * ({@link HarvesterRenderer.HarvesterRenderState#angle}) instead of in {@code HarvesterRenderer#transform}, so the
 * smoothed sub-level speed is applied to the extracted angle.
 */
@Mixin(HarvesterRenderer.class)
public class HarvesterRendererMixin {

    @Inject(method = "extractRenderState(Lcom/zurrtum/create/content/contraptions/actors/harvester/HarvesterBlockEntity;Lcom/zurrtum/create/client/content/contraptions/actors/harvester/HarvesterRenderer$HarvesterRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"))
    public void sable$smoothSpeed(final HarvesterBlockEntity be, final HarvesterRenderer.HarvesterRenderState state, final float pt, final Vec3 cameraPos, final ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, final CallbackInfo ci) {
        if (be.getAnimatedSpeed() == 0) { // use our own transformation
            state.angle = AngleHelper.rad(-((HarvesterLerpedSpeed) be).sable$getLerpedFloat().getValue(pt));
        }
    }
}
