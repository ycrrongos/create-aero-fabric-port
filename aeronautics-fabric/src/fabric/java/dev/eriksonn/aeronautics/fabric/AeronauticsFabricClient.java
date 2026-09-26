package dev.eriksonn.aeronautics.fabric;

import dev.eriksonn.aeronautics.AeronauticsClient;
import net.fabricmc.api.ClientModInitializer;

public final class AeronauticsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AeronauticsClient.init();
    }
}
