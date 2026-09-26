package com.zurrtum.create.foundation.block;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/** Compat stub of Create's DyedBlockList for Create Fly. */
public class DyedBlockList<T extends Block> {
    private final Map<DyeColor, BlockEntry<T>> map = new EnumMap<>(DyeColor.class);

    public DyedBlockList(Function<DyeColor, BlockEntry<T>> factory) {
        for (DyeColor color : DyeColor.values()) {
            map.put(color, factory.apply(color));
        }
    }

    public BlockEntry<T> get(DyeColor color) {
        return map.get(color);
    }

    public boolean contains(Block block) {
        for (BlockEntry<T> e : map.values()) {
            if (e.get() == block) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public BlockEntry<T>[] toArray() {
        return map.values().toArray(BlockEntry[]::new);
    }
}
