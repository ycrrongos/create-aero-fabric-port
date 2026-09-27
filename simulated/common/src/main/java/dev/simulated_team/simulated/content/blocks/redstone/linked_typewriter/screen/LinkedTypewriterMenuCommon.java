package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen;

import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class LinkedTypewriterMenuCommon extends AbstractContainerMenu {
    public LinkedTypewriterMenuCommon(MenuType<?> type, int id, Inventory inv, LinkedTypewriterBlockEntity be) {
        super(type, id);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }
}
