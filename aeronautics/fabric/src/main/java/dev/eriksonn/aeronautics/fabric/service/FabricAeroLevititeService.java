package dev.eriksonn.aeronautics.fabric.service;

import dev.eriksonn.aeronautics.service.AeroLevititeService;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Levitite fluids are NeoForge-only today — stub until Fabric fluid registration lands. */
public class FabricAeroLevititeService implements AeroLevititeService {
	@Override public Item getBucket() { return Items.BUCKET; }
	@Override public Fluid getFluid() { return Fluids.WATER; }
}
