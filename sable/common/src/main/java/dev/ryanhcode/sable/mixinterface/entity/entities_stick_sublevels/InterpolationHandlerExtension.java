package dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels;

public interface InterpolationHandlerExtension {

    /**
     * @return the configured amount of steps a new interpolation takes
     */
    int sable$getInterpolationSteps();

    /**
     * @return the remaining steps of the currently active interpolation, or 0 if there is none
     */
    int sable$getActiveSteps();
}
