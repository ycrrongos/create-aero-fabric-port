package dev.eriksonn.aeronautics.fabric;

import dev.eriksonn.aeronautics.Aeronautics;
import net.fabricmc.api.ModInitializer;

public final class AeronauticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Aeronautics.init();
    }
}
