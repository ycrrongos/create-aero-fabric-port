package dev.eriksonn.aeronautics;

import dev.eriksonn.aeronautics.registry.AeroRegistrate;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class AeronauticsClient {
	private AeronauticsClient() {}

	public static void init() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			return;
		}
		// Client registration re-enabled after core gameplay compiles.
	}
}
