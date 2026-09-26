package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Compat shim: upstream AllTags split into AllBlockTags / AllItemTags in Create Fly. */
public final class AllTags {
    private AllTags() {}

    public static TagKey<Item> commonItemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    public static TagKey<Block> commonBlockTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
    }

    /** Nested facade preserving AllTags.AllBlockTags.X.tag usage. */
    public static final class AllBlockTags {
        public static final TaggedBlock BRITTLE = new TaggedBlock(com.zurrtum.create.AllBlockTags.BRITTLE);
        public static final TaggedBlock SAFE_NBT = new TaggedBlock(com.zurrtum.create.AllBlockTags.SAFE_NBT);
        public static final TaggedBlock NON_MOVABLE = new TaggedBlock(com.zurrtum.create.AllBlockTags.NON_MOVABLE);
        public static final TaggedBlock WINDMILL_SAILS = new TaggedBlock(com.zurrtum.create.AllBlockTags.WINDMILL_SAILS);
        public static final TaggedBlock FAN_TRANSPARENT = new TaggedBlock(com.zurrtum.create.AllBlockTags.FAN_TRANSPARENT);

        public record TaggedBlock(TagKey<Block> tag) {
            public boolean matches(BlockState state) { return state.is(tag); }
            public boolean matches(Block block) { return block.builtInRegistryHolder().is(tag); }
        }
    }

    public static final class AllItemTags {
        public static final TaggedItem CHAIN_RIDEABLE = new TaggedItem(com.zurrtum.create.AllItemTags.CHAIN_RIDEABLE);

        public record TaggedItem(TagKey<Item> tag) {
            public boolean matches(ItemStack stack) { return stack.is(tag); }
            public boolean matches(Item item) { return item.builtInRegistryHolder().is(tag); }
        }
    }
}
