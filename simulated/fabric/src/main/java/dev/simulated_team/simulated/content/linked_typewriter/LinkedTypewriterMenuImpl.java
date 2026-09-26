package dev.simulated_team.simulated.content.linked_typewriter;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LinkedTypewriterMenuImpl extends LinkedTypewriterMenuCommon {

    public LinkedTypewriterMenuImpl(final MenuType<?> type, final int id, final Inventory inv, final RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public LinkedTypewriterMenuImpl(final MenuType<?> type, final int id, final Inventory inv, final LinkedTypewriterBlockEntity be) {
        super(type, id, inv, be);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(2);
    }

    @Override
    protected void addSlots() {
        this.addPlayerSlots(6 + (16 * 2), 11 + (16 * 3));
        for (int i = 0; i < 2; i++) {
            final int index = i;
            this.addSlot(new Slot(this.ghostInventory, i, 105 + (i * 18), 1) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return true;
                }

                @Override
                public boolean isActive() {
                    return LinkedTypewriterMenuImpl.this.slotsActive;
                }
            });
        }
    }
}
