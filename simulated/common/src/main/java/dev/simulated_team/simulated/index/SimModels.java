package dev.simulated_team.simulated.index;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;

/** Gives item-model lambdas a real provider, so modLoc and getName resolve. */
public final class SimModels {
    private SimModels() {}

    public static <T extends Item> BiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> items(
            BiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> callback) {
        return callback;
    }
}
