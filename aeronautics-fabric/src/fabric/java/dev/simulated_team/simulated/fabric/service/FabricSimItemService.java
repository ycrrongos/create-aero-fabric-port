package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.world.item.ItemStack;

public class FabricSimItemService implements SimItemService {
	@Override
	public int getBurnTime(final ItemStack stack) {
		return 0;
	}

	@Override
	public int getSuperheatedBurnTime(final ItemStack stack) {
		return 0;
	}
}
