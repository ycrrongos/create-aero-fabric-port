package dev.simulated_team.simulated.fabric.service;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerShaftBlock;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.data.SimBlockStateGen;
import dev.simulated_team.simulated.service.SimBlockStateService;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Datagen no-ops on Fabric spike (assets already generated upstream). */
public class FabricSimBlockStateService implements SimBlockStateService {
	@Override
	public <T extends Block> void genericModelBuilder(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov,
			final Function<BlockState, SimBlockStateGen.XYHolder> xyGetter, final Function<BlockState, Object> modelGetter) {}

	@Override
	public <P extends AugerShaftBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> augerShaftGenerate(final String name, final boolean cog) {
		return (ctx, prov) -> {};
	}

	@Override
	public <T extends AbstractDirectionalAxisBlock> void directionalAxisBlock(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov,
			final BiFunction<BlockState, Boolean, Object> modelFunc) {}
}
