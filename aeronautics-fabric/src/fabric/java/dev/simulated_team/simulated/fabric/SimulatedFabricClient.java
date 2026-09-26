package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.SimulatedClient;
import net.fabricmc.api.ClientModInitializer;

public final class SimulatedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SimulatedClient.init();
    }
}
