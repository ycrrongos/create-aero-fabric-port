package com.tterrag.registrate.providers;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/** Static stand-in for Registrate's recipe-unlock helper. The shim jar does not ship this class. */
public final class RegistrateRecipeProvider {
    private RegistrateRecipeProvider() {}

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(final ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(final TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, tag));
    }
}
