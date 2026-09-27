package dev.simulated_team.simulated.index;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
public class SimAdvancements {
    public static final Adv A_CALCULATED_CONNECTION = new Adv();
    public static class Adv {
        public void awardToNearby(BlockPos pos, Level level, float range) {}
    }
}
