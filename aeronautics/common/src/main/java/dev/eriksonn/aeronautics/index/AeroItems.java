package dev.eriksonn.aeronautics.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.eriksonn.aeronautics.content.items.AviatorsGogglesItem;
import dev.eriksonn.aeronautics.registry.AeroRegistrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/** Slim registration — avoid ItemBuilder.properties/tag/lang (railways vs shim signature drift). */
public class AeroItems {
	private static final AeroRegistrate REGISTRATE = Aeronautics.getRegistrate();

	public static final ItemEntry<AviatorsGogglesItem> AVIATORS_GOGGLES = REGISTRATE
			.item("aviators_goggles", AviatorsGogglesItem::new)
			.register();

	public static final ItemEntry<Item> MUSIC_DISC_CLOUD_SKIPPER = REGISTRATE
			.item("music_disc_cloud_skipper", props -> new Item(props
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, Aeronautics.path("cloud_skipper")))
					.component(AeroDataComponents.LEVITATING, Levitating.DEFAULT)))
			.register();

	public static final ItemEntry<Item> ENDSTONE_POWDER = REGISTRATE
			.item("end_stone_powder", props -> new Item(props
					.component(AeroDataComponents.LEVITATING, Levitating.END_STONE)))
			.register();

	public static void init() {}
}
