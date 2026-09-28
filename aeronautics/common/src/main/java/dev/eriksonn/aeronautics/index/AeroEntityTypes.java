package dev.eriksonn.aeronautics.index;

import com.tterrag.registrate.util.entry.EntityEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.hot_air.gust.GustEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.contraption.PropellerBearingContraptionEntity;
import dev.eriksonn.aeronautics.registry.AeroRegistrate;
import net.minecraft.world.entity.MobCategory;

public class AeroEntityTypes {
	private static final AeroRegistrate REGISTRATE = Aeronautics.getRegistrate();

	public static final EntityEntry<PropellerBearingContraptionEntity> PROPELLER_CONTROLLED_CONTRAPTION =
			REGISTRATE.entity("propeller_bearing_contraption", PropellerBearingContraptionEntity::new, MobCategory.MISC)
					.register();

	public static final EntityEntry<GustEntity> GUST =
			REGISTRATE.<GustEntity>entity("gust", GustEntity::new, MobCategory.MISC)
					.register();

	public static void init() {}
}
