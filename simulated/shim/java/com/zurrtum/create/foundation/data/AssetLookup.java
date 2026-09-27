package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.modelgen.ModelFile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

/** Subset of Create's AssetLookup that Create Fly no longer ships. */
public final class AssetLookup {
    private AssetLookup() {}

    public static Function<BlockState, ModelFile> partialBaseModel(final DataGenContext<Block, ?> ctx, final RegistrateBlockstateProvider prov) {
        return state -> new ModelFile();
    }

    public static Function<BlockState, ModelFile> forPowered(final DataGenContext<Block, ?> ctx, final RegistrateBlockstateProvider prov) {
        return state -> new ModelFile();
    }

    public static Object itemModelWithPartials() {
        return new ModelFile();
    }
}
