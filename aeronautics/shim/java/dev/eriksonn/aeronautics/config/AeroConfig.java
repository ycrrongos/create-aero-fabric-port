package dev.eriksonn.aeronautics.config;

import dev.eriksonn.aeronautics.config.client.AeroClient;
import dev.eriksonn.aeronautics.config.server.AeroServer;

public interface AeroConfig {
	AeroServer getServerConfig();
	AeroClient getClientConfig();
}
