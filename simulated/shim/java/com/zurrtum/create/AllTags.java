package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Minimal AllTags stand-in for Create Fly (package layout differs). */
public class AllTags {
    public static TagKey<Item> commonItemTag(final String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    public static TagKey<Block> commonBlockTag(final String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
    }

    public static class AllItemTags {
        public static final NamedTag<Item> CHAIN_RIDEABLE = new NamedTag<>(commonItemTag("chain_rideable"));
        public static final NamedTag<Item> WRENCH = new NamedTag<>(commonItemTag("tools/wrench"));
    }

    public static class AllBlockTags {
        public static final NamedTag<Block> BRITTLE = new NamedTag<>(commonBlockTag("brittle"));
        public static final NamedTag<Block> SAFE_NBT = new NamedTag<>(commonBlockTag("safe_nbt"));
    }

    public static class NamedTag<T> {
        public final TagKey<T> tag;
        public NamedTag(final TagKey<T> tag) { this.tag = tag; }
        public boolean matches(final ItemStack stack) {
            return stack.is(TagKey.create(Registries.ITEM, tag.location()));
        }
        public boolean matches(final BlockState state) {
            return state.is(TagKey.create(Registries.BLOCK, tag.location()));
        }
        public boolean isIn(final ItemStack stack) { return matches(stack); }
        public TagKey<T> tag() { return tag; }
    }
}
