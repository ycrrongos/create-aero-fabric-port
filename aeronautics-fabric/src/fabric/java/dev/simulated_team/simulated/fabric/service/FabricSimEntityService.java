package dev.simulated_team.simulated.fabric.service;

import com.tterrag.registrate.builders.EntityBuilder;
import dev.simulated_team.simulated.index.SimEntityTypes;
import dev.simulated_team.simulated.service.SimEntityService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class FabricSimEntityService implements SimEntityService {
	private static final CompoundTag EMPTY = new CompoundTag();

	@Override
	public CompoundTag getCustomData(final Entity entity) {
		// NeoForge persistent data — Fabric uses components/cardinal; stub empty for compile/runtime spike
		return EMPTY;
	}

	@Override
	public double getPlayerReach(final Player player) {
		return player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
	}

	@Override
	public boolean isFake(final Player player) {
		return false;
	}

	@Override
	public <T extends Entity, P> EntityBuilder<T, P> loaderEntityTransform(final EntityBuilder<T, P> builder, final SimEntityTypes.EntityLoaderData data) {
		return builder.properties(p -> {
			if (data.immuneToFire()) p.fireImmune();
			p.sized(data.width(), data.height());
		});
	}
}
