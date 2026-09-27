package dev.eriksonn.aeronautics.config.server;

import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.infrastructure.config.CStress;
import dev.eriksonn.aeronautics.fabric.service.FabricAeroConfigService;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AeroServer extends ConfigBase implements FabricAeroConfigService.AeroConfigHolder {
	private ModConfigSpec specification;
	public final Kinetics kinetics = new Kinetics();

	@Override
	public String getName() {
		return "server";
	}

	@Override
	public void aeronautics$setSpecification(final ModConfigSpec specification) {
		this.specification = specification;
	}

	public static final class Kinetics {
		public final CStress stressValues = new CStress();
	}
}
