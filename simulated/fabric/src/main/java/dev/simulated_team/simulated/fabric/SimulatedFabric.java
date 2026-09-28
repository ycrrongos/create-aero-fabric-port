package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SimulatedFabric implements ModInitializer {
    public static CreativeModeTab TAB;

    @Override
    public void onInitialize() {
        TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(Simulated.MOD_ID, "main_tab"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup." + Simulated.MOD_ID + ".group"))
                        .icon(SimulatedFabric::tabIcon)
                        .displayItems(SimulatedFabric::buildContents)
                        .build()
        );
        Simulated.init();
    }

    private static ItemStack tabIcon() {
        Item preferred = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("aeronautics", "hot_air_burner")).orElse(null);
        if (preferred != null && preferred != Items.AIR) {
            return new ItemStack(preferred);
        }
        preferred = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(Simulated.MOD_ID, "steering_wheel")).orElse(null);
        if (preferred != null && preferred != Items.AIR) {
            return new ItemStack(preferred);
        }
        return new ItemStack(Items.COMPASS);
    }

    private static void buildContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier id = entry.getKey().identifier();
            String ns = id.getNamespace();
            if ("simulated".equals(ns) || "aeronautics".equals(ns)) {
                output.accept(entry.getValue());
            }
        }
    }
}
