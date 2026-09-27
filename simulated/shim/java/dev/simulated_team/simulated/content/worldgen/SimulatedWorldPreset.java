package dev.simulated_team.simulated.content.worldgen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SimulatedWorldPreset {
    public SimulatedWorldPreset(Identifier id, Component description) {}

    public Identifier id() {
        return Identifier.withDefaultNamespace("airship_ready");
    }
}
