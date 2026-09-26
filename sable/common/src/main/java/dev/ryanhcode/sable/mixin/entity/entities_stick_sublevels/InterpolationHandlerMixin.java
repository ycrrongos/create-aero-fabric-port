package dev.ryanhcode.sable.mixin.entity.entities_stick_sublevels;

import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.InterpolationHandlerExtension;
import net.minecraft.world.entity.InterpolationHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InterpolationHandler.class)
public class InterpolationHandlerMixin implements InterpolationHandlerExtension {

    @Shadow
    private int interpolationSteps;

    @Shadow
    @Final
    private InterpolationHandler.InterpolationData interpolationData;

    @Override
    public int sable$getInterpolationSteps() {
        return this.interpolationSteps;
    }

    @Override
    public int sable$getActiveSteps() {
        return Math.max(this.interpolationData.steps, 0);
    }
}
