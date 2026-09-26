package dev.simulated_team.simulated.fabric.service;

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.infrastructure.config.CStress;
import dev.simulated_team.simulated.config.client.SimClient;
import dev.simulated_team.simulated.config.server.SimServer;
import dev.simulated_team.simulated.service.SimConfigService;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public class FabricSimConfigService implements SimConfigService {
	private static SimServer server;
	private static SimClient client;

	static {
		server = register(SimServer::new, ModConfig.Type.SERVER, "simulated-server.toml");
		client = register(SimClient::new, ModConfig.Type.CLIENT, "simulated-client.toml");
		try {
			final CStress stress = server.kinetics.stressValues;
			BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
			BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
		} catch (Throwable t) {
			// Create Fly stress API may differ — continue without providers
		}
	}

	private static <T extends com.zurrtum.create.catnip.config.ConfigBase> T register(final Supplier<T> factory, final ModConfig.Type side, final String file) {
		final Pair<T, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(builder -> {
			final T config = factory.get();
			config.registerAll(builder);
			return config;
		});
		final T config = pair.getLeft();
		config.specification = pair.getRight();
		ConfigRegistry.INSTANCE.register("simulated", side, config.specification, file);
		return config;
	}

	@Override public boolean serverLoaded() { return server != null && server.specification != null && server.specification.isLoaded(); }
	@Override public boolean clientLoaded() { return client != null && client.specification != null && client.specification.isLoaded(); }
	@Override public SimServer server() { return server; }
	@Override public SimClient client() { return client; }
}
