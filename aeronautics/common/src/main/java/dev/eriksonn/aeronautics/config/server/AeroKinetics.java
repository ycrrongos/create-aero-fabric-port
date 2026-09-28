package dev.eriksonn.aeronautics.config.server;

import com.zurrtum.create.catnip.config.ConfigBase;

public class AeroKinetics extends ConfigBase {
    public final AeroStress stressValues = this.nested(1, AeroStress::new, "Fine tune the kinetic stats of individual components");

    @Override
    public String getName() {
        return "kinetics";
    }
}
