package com.zurrtum.create.foundation.data;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.level.block.Block;
public class BlockStateGen {
  public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> axisBlockProvider(boolean horizontal) { return (c,p)->{}; }
  public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalBlockProvider(boolean horizontal) { return (c,p)->{}; }
  public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockProvider(boolean horizontal) { return (c,p)->{}; }
  public static Object horizontalBlock(Object... a) { return null; }
  public static Object axisBlock(Object... a) { return null; }
}
