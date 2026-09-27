package dev.simulated_team.simulated.registrate;

import com.zurrtum.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Minimal Create Fly registrate facade. */
public class SimulatedRegistrate extends CreateRegistrate {
    public SimulatedRegistrate(Identifier id, String modid) {
        super(modid);
    }

    public SimulatedRegistrate(String modid) {
        super(modid);
    }

    public SimulatedRegistrate defaultCreativeTab(ResourceKey<?> key) {
        return this;
    }
}
