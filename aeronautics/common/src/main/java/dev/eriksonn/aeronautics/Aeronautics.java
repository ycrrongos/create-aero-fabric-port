package dev.eriksonn.aeronautics;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.eriksonn.aeronautics.events.AeronauticsCommonEvents;
import dev.eriksonn.aeronautics.index.*;
import dev.eriksonn.aeronautics.network.AeroPacketManager;
import dev.eriksonn.aeronautics.registry.AeroRegistrate;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aeronautics {
	public static final String MOD_ID = "aeronautics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final AeroRegistrate REGISTRATE_INSTANCE =
			new AeroRegistrate(Aeronautics.path("aeronautics"), MOD_ID);
	private static final NonNullSupplier<AeroRegistrate> REGISTRATE = () -> REGISTRATE_INSTANCE;

	public static void init() {
		safe("AeroBlocks", AeroBlocks::init);
		safe("AeroBlockEntityTypes", AeroBlockEntityTypes::init);
		safe("AeroItems", AeroItems::init);
		safe("AeroEntityTypes", AeroEntityTypes::init);
		safe("AeroArmorMaterials", AeroArmorMaterials::init);
		safe("AeroSoundEvents", AeroSoundEvents::init);
		safe("AeroLiftingGasTypes", AeroLiftingGasTypes::init);
		safe("AeroBlockMovementChecks", AeroBlockMovementChecks::init);
		safe("AeroRegistries", AeroRegistries::init);
		safe("AeroPacketManager", AeroPacketManager::init);
		safe("AeroLevititeBlendPropagationContexts", AeroLevititeBlendPropagationContexts::init);
		safe("AeroDataComponents", AeroDataComponents::init);
		listenCommonEvents();
	}

	private static void safe(final String name, final Runnable r) {
		try {
			r.run();
		} catch (Throwable t) {
			LOGGER.error("Aeronautics init step '{}' failed: {}", name, t.toString());
			t.printStackTrace();
		}
	}

	private static void listenCommonEvents() {
		try {
			SableEventPlatform.INSTANCE.onPhysicsTick(AeronauticsCommonEvents::physicsTick);
			SableEventPlatform.INSTANCE.onSubLevelContainerReady(AeronauticsCommonEvents::onSubLevelContainerReady);
		} catch (Throwable t) {
			LOGGER.warn("Sable common event hooks skipped: {}", t.toString());
		}
	}

	public static AeroRegistrate getRegistrate() {
		return REGISTRATE.get();
	}

	public static Identifier path(final String path) {
		return Identifier.tryBuild(MOD_ID, path);
	}
}
