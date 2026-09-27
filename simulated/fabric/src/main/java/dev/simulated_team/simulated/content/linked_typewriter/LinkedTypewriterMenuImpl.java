package dev.simulated_team.simulated.content.linked_typewriter;

import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuCommon;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class LinkedTypewriterMenuImpl extends LinkedTypewriterMenuCommon {
    public LinkedTypewriterMenuImpl(MenuType<?> type, int id, Inventory inv, LinkedTypewriterBlockEntity be) {
        super(type, id, inv, be);
    }
}
