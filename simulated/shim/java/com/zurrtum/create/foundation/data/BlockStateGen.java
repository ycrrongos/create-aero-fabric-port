package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.modelgen.ModelFile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Subset of Create's BlockStateGen. Bodies are empty; call sites only need the types. */
public final class BlockStateGen {
    private BlockStateGen() {}

    public static void axisBlock(final DataGenContext<Block, ?> ctx, final RegistrateBlockstateProvider prov, final Function<BlockState, ModelFile> model) {}

    public static void directionalAxisBlock(final DataGenContext<Block, ?> ctx, final RegistrateBlockstateProvider prov, final BiFunction<BlockState, Boolean, ModelFile> model) {}

    public static Object horizontalAxisBlockProvider(final boolean includeItem) {
        return (com.tterrag.registrate.util.nullness.NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider>) (ctx, prov) -> {};
    }
}
