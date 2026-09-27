package com.zurrtum.create.foundation.data.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Common-metal item tags Create used to ship. Create Fly no longer has this enum. */
public enum CommonMetal {
    IRON("iron"),
    COPPER("copper"),
    GOLD("gold"),
    BRASS("brass");

    public final TagKey<Item> ingots;
    public final TagKey<Item> nuggets;
    public final TagKey<Item> plates;

    CommonMetal(final String name) {
        this.ingots = item("ingots/" + name);
        this.nuggets = item("nuggets/" + name);
        this.plates = item("plates/" + name);
    }

    private static TagKey<Item> item(final String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }
}
