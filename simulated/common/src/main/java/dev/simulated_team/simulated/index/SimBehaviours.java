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

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> displaySource(RegistryEntry<? extends DisplaySource, ?> source) {
        return builder -> builder.onRegister(block -> DisplaySource.BY_BLOCK.add(block, source.get()));
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> displayTarget(RegistryEntry<? extends DisplayTarget, ?> target) {
        return builder -> builder.onRegister(block -> DisplayTarget.BY_BLOCK.register(block, target.get()));
    }
}
