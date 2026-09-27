package com.zurrtum.create.foundation.block;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import java.util.EnumMap;
import java.util.function.Function;

/** Create Fly dropped this helper. Enough of it for Simulated's dyed block lists. */
public final class DyedBlockList<T extends Block> {
    private final EnumMap<DyeColor, BlockEntry<T>> map = new EnumMap<>(DyeColor.class);

    public DyedBlockList(final Function<DyeColor, BlockEntry<T>> factory) {
        for (final DyeColor color : DyeColor.values()) {
            this.map.put(color, factory.apply(color));
        }
    }

    public BlockEntry<T> get(final DyeColor color) {
        return this.map.get(color);
    }
}
