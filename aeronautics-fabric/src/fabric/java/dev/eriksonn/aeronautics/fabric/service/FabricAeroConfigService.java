package dev.eriksonn.aeronautics.fabric.service;

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.infrastructure.config.CStress;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.config.client.AeroClient;
import dev.eriksonn.aeronautics.config.server.AeroServer;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public class FabricAeroConfigService implements AeroConfig {
	private static AeroServer server;
	private static AeroClient client;

	static {
		server = register(AeroServer::new, ModConfig.Type.SERVER, "aeronautics-server.toml");
		client = register(AeroClient::new, ModConfig.Type.CLIENT, "aeronautics-client.toml");
		try {
			final CStress stress = server.kinetics.stressValues;
			BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
			BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
		} catch (Throwable ignored) {}
	}

	private static <T extends com.zurrtum.create.catnip.config.ConfigBase> T register(final Supplier<T> factory, final ModConfig.Type side, final String file) {
		final Pair<T, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(builder -> {
			final T config = factory.get();
			config.registerAll(builder);
			return config;
		});
		final T config = pair.getLeft();
		config.specification = pair.getRight();
		ConfigRegistry.INSTANCE.register("aeronautics", side, config.specification, file);
		return config;
	}

	@Override public AeroServer getServerConfig() { return server; }
	@Override public AeroClient getClientConfig() { return client; }
}
