package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.Block;

/** Minimal ModelGen stand-in for Create Fly. */
public class ModelGen {
    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> customItemModel() {
        return b -> b;
    }
    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> customItemModel(String... path) {
        return b -> b;
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> axeOrPickaxe() {
        return b -> b;
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> pickaxeOnly() {
        return b -> b;
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> axeOnly() {
        return b -> b;
    }
}
