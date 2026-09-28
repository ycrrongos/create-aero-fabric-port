package dev.eriksonn.aeronautics.fabric.service;

import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.config.client.AeroClient;
import dev.eriksonn.aeronautics.config.server.AeroServer;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FabricAeroConfigService implements AeroConfig {
	private static AeroServer server;
	private static AeroClient client;

	static {
		server = registerServer();
		client = registerClient();
		// Create Fly stress wiring deferred — AeroStress is not CStress.
		
	}

	private static AeroServer registerServer() {
		final Pair<AeroServer, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(builder -> new AeroServer());
		final AeroServer config = pair.getLeft();
		((AeroConfigHolder) (Object) config).aeronautics$setSpecification(pair.getRight());
		ConfigRegistry.INSTANCE.register("aeronautics", ModConfig.Type.SERVER, pair.getRight(), "aeronautics-server.toml");
		return config;
	}

	private static AeroClient registerClient() {
		final Pair<AeroClient, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(builder -> new AeroClient());
		final AeroClient config = pair.getLeft();
		((AeroConfigHolder) (Object) config).aeronautics$setSpecification(pair.getRight());
		ConfigRegistry.INSTANCE.register("aeronautics", ModConfig.Type.CLIENT, pair.getRight(), "aeronautics-client.toml");
		return config;
	}

	@Override public AeroServer getServerConfig() { return server; }
	@Override public AeroClient getClientConfig() { return client; }

	/** Bridges ConfigBase (no spec field) and ForgeConfigAPIPort registration. */
	public interface AeroConfigHolder {
		void aeronautics$setSpecification(ModConfigSpec specification);
	}
}
