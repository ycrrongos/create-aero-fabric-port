package dev.simulated_team.simulated.fabric.service;

import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import dev.simulated_team.simulated.multiloader.energy.SingleBattery;
import dev.simulated_team.simulated.multiloader.inventory.AbstractContainer;
import dev.simulated_team.simulated.multiloader.inventory.InventoryLoaderWrapper;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import dev.simulated_team.simulated.service.SimInventoryService;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/** Stub inventory bridge — Transfer API wiring deferred. */
public class FabricSimInventoryService implements SimInventoryService {
	@Override
	public <T extends InventoryLoaderWrapper> T getInventory(@Nullable final BlockEntity be, @Nullable final Direction dir) {
		return null;
	}

	@Override
	public <T extends InventoryLoaderWrapper> T getWrappedAllItemsFromContraption(final MountedStorageManager manager) {
		return null;
	}

	@Override
	public <T extends InventoryLoaderWrapper> T getWrappedMountedItemsFromContraption(final MountedStorageManager manager) {
		return null;
	}

	@Override
	public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerInventory(final BiFunction<T, Direction, AbstractContainer> getter) {
		return type -> {};
	}

	@Override
	public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerTank(final BiFunction<T, Direction, SingleTank> getter) {
		return type -> {};
	}

	@Override
	public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerBattery(final BiFunction<T, Direction, SingleBattery> getter) {
		return type -> {};
	}
}
