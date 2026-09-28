package dev.eriksonn.aeronautics.config.server;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.zurrtum.create.catnip.config.ConfigBase;
import net.minecraft.world.level.block.Block;

public class AeroStress extends ConfigBase {
    public AeroStress() {}

    @Override
    public String getName() {
        return "stress";
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(final double value) {
        return builder -> builder;
    }
}
