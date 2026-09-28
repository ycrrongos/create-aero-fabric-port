package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiPredicate;

/** Port shim mirroring Create Fly BiPredicate constructor. */
public class DirectionalExtenderScrollOptionSlot extends ValueBoxTransform.Sided {
    private final BiPredicate<BlockState, Direction> allowedDirections;

    public DirectionalExtenderScrollOptionSlot(final BiPredicate<BlockState, Direction> allowedDirections) {
        this.allowedDirections = allowedDirections;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }

    @Override
    protected boolean isSideActive(final BlockState state, final Direction direction) {
        return this.allowedDirections.test(state, direction);
    }
}
