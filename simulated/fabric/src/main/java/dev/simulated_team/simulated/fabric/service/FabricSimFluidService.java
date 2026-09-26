package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimFluidService;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FabricSimFluidService implements SimFluidService {
	@Override
	public long mbToLoaderUnits(final long mb) {
		return mb * 81L; // Fabric fluid units
	}

	@Override
	public Fluid getFluidInItem(final ItemStack stack) {
		return Fluids.EMPTY;
	}
}
