package dev.simulated_team.simulated.index;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Minimal click-interaction facade until client managers are restored. */
public class SimClickInteractions {
    public static final HoldHandler HANDLE_HANDLER = new HoldHandler();
    public static final HoldHandler STEERING_WHEEL_MANAGER = new HoldHandler();
    public static final HoldHandler THROTTLE_LEVER_MANAGER = new HoldHandler();

    public static class HoldHandler {
        public void startHold(Level level, Player player, BlockPos pos) {}
        public boolean isActive() { return false; }
    }
}
