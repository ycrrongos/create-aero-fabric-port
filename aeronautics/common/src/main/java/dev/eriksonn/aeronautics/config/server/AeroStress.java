package dev.eriksonn.aeronautics.config.server;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.Block;

/** Stress config deferred on Fabric — blocks register with NeoForge-era helpers only. */
public final class AeroStress {
    private AeroStress() {}

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(final double value) {
        return builder -> builder;
    }
}
