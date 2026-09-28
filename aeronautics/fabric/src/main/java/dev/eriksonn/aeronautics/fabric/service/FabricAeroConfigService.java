package dev.eriksonn.aeronautics.fabric.service;

import com.zurrtum.create.catnip.config.Builder;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.config.client.AeroClient;
import dev.eriksonn.aeronautics.config.server.AeroServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/** Create Fly JSON config via catnip Builder (not ForgeConfigAPIPort ModConfigSpec). */
public class FabricAeroConfigService implements AeroConfig {
	private static final AeroServer SERVER = Builder.create(AeroServer::new, "aeronautics", "server");
	private static final AeroClient CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
			? Builder.create(AeroClient::new, "aeronautics", "client")
			: new AeroClient();

	@Override
	public AeroServer getServerConfig() {
		return SERVER;
	}

	@Override
	public AeroClient getClientConfig() {
		return CLIENT;
	}
}
