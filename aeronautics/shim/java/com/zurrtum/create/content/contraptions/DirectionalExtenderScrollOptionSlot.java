package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiPredicate;

public class DirectionalExtenderScrollOptionSlot extends ValueBoxTransform.Sided {
    private final BiPredicate<BlockState, Direction> allowed;

    public DirectionalExtenderScrollOptionSlot(final BiPredicate<BlockState, Direction> allowedDirections) {
        this.allowed = allowedDirections;
    }

    @Override
    public Vec3 getLocalOffset(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        return Vec3.ZERO;
    }
}
