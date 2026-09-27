package dev.simulated_team.simulated.config.server.blocks;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.zurrtum.create.catnip.config.ConfigBase;
import net.minecraft.world.level.block.Block;

/** Kinetics stress config + registrate helpers (Create Fly port stub). */
public class SimStress extends ConfigBase {

    @Override
    public String getName() {
        return "stress";
    }

    public static void register() {}

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> setImpact(final double impact) {
        return b -> b;
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> setCapacity(final double capacity) {
        return b -> b;
    }
}
