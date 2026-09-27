package dev.simulated_team.simulated.index;

import com.tterrag.registrate.providers.DataGenContext;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;

/**
 * Gives recipe lambdas a real DataGenContext target. Registrate's recipe method is BiConsumer of wildcards,
 * which hides get().
 */
public final class SimRecipes {
    private SimRecipes() {}

    public static <T extends Block> BiConsumer<DataGenContext<Block, T>, RecipeOutput> blocks(
            BiConsumer<DataGenContext<Block, T>, RecipeOutput> callback) {
        return callback;
    }

    public static <T extends Item> BiConsumer<DataGenContext<Item, T>, RecipeOutput> items(
            BiConsumer<DataGenContext<Item, T>, RecipeOutput> callback) {
        return callback;
    }
}
