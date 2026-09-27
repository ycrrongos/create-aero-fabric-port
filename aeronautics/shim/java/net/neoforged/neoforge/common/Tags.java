package net.neoforged.neoforge.common;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
/** Compile stub mapping NeoForge Tags to conventional `c:` tags. */
public final class Tags {
  private Tags(){}
  private static TagKey<Item> item(String path){ return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path)); }
  private static TagKey<Block> block(String path){ return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path)); }
  public static final class Items {
    public static final TagKey<Item> DUSTS_REDSTONE = item("dusts/redstone");
    public static final TagKey<Item> STRINGS = item("strings");
    public static final TagKey<Item> NUGGETS_IRON = item("nuggets/iron");
    public static final TagKey<Item> ENCHANTABLES = item("enchantables");
    public static final TagKey<Item> GEMS_AMETHYST = item("gems/amethyst");
    private Items(){}
  }
  public static final class Blocks {
    public static final TagKey<Block> STORAGE_BLOCKS_COAL = block("storage_blocks/coal");
    private Blocks(){}
  }
}
