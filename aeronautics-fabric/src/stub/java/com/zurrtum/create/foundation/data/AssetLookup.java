package com.zurrtum.create.foundation.data;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.level.block.Block;
public class AssetLookup {
  public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> standardBlock() { return (c,p)->{}; }
  public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> partialBaseModel() { return (c,p)->{}; }
  public static Object partialBaseModel(Object... a) { return null; }
  public static Object standardModel(Object... a) { return null; }
  public static Object customItemModel(Object... a) { return null; }
}
