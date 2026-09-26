package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import dev.simulated_team.simulated.index.SimBlocks;

public final class SimulatedFabric implements ModInitializer {
    public static CreativeModeTab TAB;

    @Override
    public void onInitialize() {
        TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(Simulated.MOD_ID, "main_tab"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup." + Simulated.MOD_ID + ".group"))
                        .icon(() -> new ItemStack(SimBlocks.PHYSICS_ASSEMBLER.get()))
                        .build()
        );
        Simulated.init();
    }
}
