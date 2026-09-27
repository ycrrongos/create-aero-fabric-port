package dev.eriksonn.aeronautics.config.client;

import com.zurrtum.create.catnip.config.ConfigBase;
import dev.eriksonn.aeronautics.fabric.service.FabricAeroConfigService;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AeroClient extends ConfigBase implements FabricAeroConfigService.AeroConfigHolder {
	private ModConfigSpec specification;

	@Override
	public String getName() {
		return "client";
	}

	@Override
	public void aeronautics$setSpecification(final ModConfigSpec specification) {
		this.specification = specification;
	}
}
