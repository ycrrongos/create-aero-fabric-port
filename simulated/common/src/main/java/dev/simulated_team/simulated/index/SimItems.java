package dev.simulated_team.simulated.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.world.item.Item;

public class SimItems {
    private static final SimulatedRegistrate REGISTRATE = Simulated.getRegistrate();

    public static final ItemEntry<Item> ANDESITE_ROD = REGISTRATE.item("andesite_rod", Item::new).register();

    public static final ItemEntry<Item> BRASS_ROD = REGISTRATE.item("brass_rod", Item::new).register();

    public static final ItemEntry<Item> GYRO_MECHANISM = REGISTRATE.item("gyroscopic_mechanism", Item::new).register();

    public static void register() {}
}
