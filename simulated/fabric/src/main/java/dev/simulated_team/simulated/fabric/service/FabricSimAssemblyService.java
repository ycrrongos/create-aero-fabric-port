package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimAssemblyService;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FabricSimAssemblyService implements SimAssemblyService {
	@Override
	public boolean canStickTo(final BlockState stateA, final BlockState stateB) {
		return stateA.is(Blocks.SLIME_BLOCK) || stateA.is(Blocks.HONEY_BLOCK)
				|| stateB.is(Blocks.SLIME_BLOCK) || stateB.is(Blocks.HONEY_BLOCK);
	}
}
