package dev.simulated_team.simulated.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public final class SimLevelUtil {
    private SimLevelUtil() {}
    public static boolean isFake(Object player) { return false; }
    public static Level getLevel(LevelAccessor accessor) {
        return accessor instanceof Level level ? level : null;
    }
    public static void markDirty(Level level, BlockPos pos) {
        if (level != null) level.blockEntityChanged(pos);
    }
    public static boolean isAreaActuallyLoaded(Level level, BlockPos pos, int radius) {
        if (level == null || pos == null) return false;
        return level.isLoaded(pos);
    }
}
