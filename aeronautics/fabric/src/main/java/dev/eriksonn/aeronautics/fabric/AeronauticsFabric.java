package dev.eriksonn.aeronautics.fabric;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.events.AeronauticsCommonEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class AeronauticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Aeronautics.init();
        // NeoForge hooked these via ServerTickEvent.Post / ServerStoppedEvent; Fabric needs explicit registration.
        ServerTickEvents.END_WORLD_TICK.register(AeronauticsCommonEvents::onServerTickEnd);
        ServerLifecycleEvents.SERVER_STOPPED.register(AeronauticsCommonEvents::onServerStopped);
    }
}
