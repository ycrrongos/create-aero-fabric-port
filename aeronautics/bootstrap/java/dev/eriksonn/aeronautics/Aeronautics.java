package dev.eriksonn.aeronautics;

import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import dev.simulated_team.simulated.util.SimColors;
import dev.eriksonn.aeronautics.index.*;
import dev.eriksonn.aeronautics.network.AeroPacketManager;
import dev.eriksonn.aeronautics.registry.AeroRegistrate;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aeronautics {
	public static final String MOD_ID = "aeronautics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static AeroRegistrate REGISTRATE;

	public static void init() {
		if (REGISTRATE == null) {
			REGISTRATE = new AeroRegistrate(Aeronautics.path("aeronautics"), MOD_ID);
		}
		setTooltips();

		AeroBlocks.init();
		AeroBlockEntityTypes.init();
		AeroItems.init();
		AeroEntityTypes.init();
		AeroArmorMaterials.init();
		AeroSoundEvents.init();
		AeroLiftingGasTypes.init();
		AeroBlockMovementChecks.init();
		AeroRegistries.init();
		AeroPacketManager.init();
		AeroLevititeBlendPropagationContexts.init();
		AeroDataComponents.init();
	}

	public static void setTooltips() {
		getRegistrate().setTooltipModifierFactory(item -> {
			final Rarity rarity = item.getDefaultInstance().getRarity();
			FontHelper.Palette color = FontHelper.Palette.STANDARD_CREATE;
			if (rarity == Rarity.EPIC)
				color = new FontHelper.Palette(TooltipHelper.styleFromColor(SimColors.EPIC_OURPLE), TooltipHelper.styleFromColor(rarity.color()));

			return new ItemDescription
					.Modifier(item, color)
					.andThen(TooltipModifier.mapNull(KineticStats.create(item)));
		});
	}

	public static AeroRegistrate getRegistrate() {
		return REGISTRATE;
	}

	public static Identifier path(final String path) {
		return Identifier.tryBuild(MOD_ID, path);
	}
}
