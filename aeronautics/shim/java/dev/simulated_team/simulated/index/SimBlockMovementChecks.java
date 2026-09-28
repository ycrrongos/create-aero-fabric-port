package dev.simulated_team.simulated.index;

import com.zurrtum.create.api.contraption.BlockMovementChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Stub: full implementation excluded from simulated compile for Create Fly port. */
public final class SimBlockMovementChecks {
    private SimBlockMovementChecks() {}

    @FunctionalInterface
    public interface AttachedCheck {
        BlockMovementChecks.CheckResult check(BlockState state, Level world, BlockPos pos, Direction direction);
    }

    public static synchronized void registerAttachedCheck(final AttachedCheck check) {
        BlockMovementChecks.registerAttachedCheck(check::check);
    }
}
