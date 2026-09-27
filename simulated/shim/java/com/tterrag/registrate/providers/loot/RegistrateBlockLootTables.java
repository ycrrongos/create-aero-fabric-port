package com.tterrag.registrate.providers.loot;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/** Named stand-in for registrate block loot callbacks. */
public class RegistrateBlockLootTables {
    public void dropSelf(Block block) {}

    public void dropOther(Block block, ItemLike item) {}

    public void add(Block block, Object table) {}

    public Object createSingleItemTable(ItemLike item) {
        return null;
    }
}
