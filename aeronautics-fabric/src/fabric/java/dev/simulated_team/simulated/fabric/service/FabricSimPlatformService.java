package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimPlatformService;
import net.fabricmc.loader.api.FabricLoader;

public class FabricSimPlatformService implements SimPlatformService {
	@Override
	public boolean isLoaded(final String modId) {
		if ("simulated".equals(modId) || "aeronautics".equals(modId)) {
			return true;
		}
		return FabricLoader.getInstance().isModLoaded(modId);
	}
}
