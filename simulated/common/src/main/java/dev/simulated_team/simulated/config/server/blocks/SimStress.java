package dev.simulated_team.simulated.config.server.blocks;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.Block;

public class SimStress {
    public static void register() {}
    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> setImpact(double impact) {
        return b -> b;
    }
    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> setCapacity(double capacity) {
        return b -> b;
    }
}
