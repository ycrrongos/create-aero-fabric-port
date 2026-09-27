package dev.simulated_team.simulated.index;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import net.minecraft.world.level.block.Block;

/** Create Fly dropped the old static registrate helpers. These register on the Fly registries. */
public final class SimBehaviours {
    private SimBehaviours() {}

    public static NonNullConsumer<Block> movementBehaviour(MovementBehaviour behaviour) {
        return block -> MovementBehaviour.REGISTRY.register(block, behaviour);
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> displaySource(final Object source) {
        if (source instanceof RegistryEntry<?, ?> entry) {
            return builder -> builder.onRegister(block -> DisplaySource.BY_BLOCK.add(block, (DisplaySource) entry.get()));
        }
        return builder -> builder;
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> displayTarget(final Object target) {
        if (target instanceof RegistryEntry<?, ?> entry) {
            return builder -> builder.onRegister(block -> DisplayTarget.BY_BLOCK.register(block, (DisplayTarget) entry.get()));
        }
        return builder -> builder;
    }
}
