package dev.simulated_team.simulated.client;

import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPropertiesTooltip {
    public enum Condition {
        GOGGLES,
        ALWAYS;

        public boolean allows() {
            return this == ALWAYS;
        }
    }

    public interface TooltipFunction {
        void apply(ItemStack stack, TooltipFlag flag, java.util.List<net.minecraft.network.chat.Component> tooltip);
    }

    public static class Entry {
        public Entry(TooltipFunction function, float priority) {}
    }

    public static void init() {}

    public static void register(SimulatedRegistrate registrate, String name, TooltipFunction fn, float priority) {}

    public static boolean shouldShowTooltip(Condition condition, TooltipFlag flag, net.minecraft.world.entity.player.Player player) {
        return false;
    }

    public static void appendTooltip(BlockState state, ItemStack stack, TooltipFlag flag, java.util.List<net.minecraft.network.chat.Component> tooltip) {}
}
