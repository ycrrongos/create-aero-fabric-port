package dev.simulated_team.simulated.index;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.HashMap;
import java.util.Map;

public class SimTags {

    public static void addGenerators() {}

    public static class Blocks {
        public static final TagKey<Block> AIRTIGHT = create("aeronautics", "airtight");
        public static final TagKey<Block> SUPER_LIGHT = create("sable", "super_light");
        public static final TagKey<Block> NON_MOVABLE = create("simulated", "non_movable");
        public static final TagKey<Block> HANDLES = create("simulated", "handles");

        private static TagKey<Block> create(final String namespace, final String path) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(namespace, path));
        }
    }

    public static class Items {
        public static TagKey<Item> dyesTag(final DyeColor dyeColor) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + dyeColor.getName()));
        }
    }

    public static class Misc {
        public static final TagKey<MapDecorationType> NAV_TABLE_FINDABLE = TagKey.create(
                Registries.MAP_DECORATION_TYPE, Identifier.fromNamespaceAndPath("simulated", "nav_table_findable"));

        public static final TagKey<EntityType<?>> ARMOR_STAND_IGNORE = TagKey.create(
                Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("simulated", "armor_stand_ignore"));
    }

    public static final Map<String, TagKey<Item>> DYE_MAP = new HashMap<>();

    static {
        for (final DyeColor color : DyeColor.values()) {
            DYE_MAP.put(
                    color.getName(),
                    TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + color.getName())));
        }
    }

    public static void register() {}
}
