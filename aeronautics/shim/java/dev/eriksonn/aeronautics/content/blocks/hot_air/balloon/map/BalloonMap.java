package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap; import java.util.Map;
public class BalloonMap {
  public static final Map<Level, BalloonMap> MAP = new HashMap<>();
  public void updateNearbyBalloons(BlockPos pos, BlockState oldState, BlockState newState) {}
  public static void tick(ServerLevel level) {}
  public static void physicsTick(ServerLevel level, double timeStep) {}
  public static class BalloonSubLevelObserver {
    public BalloonSubLevelObserver(Level level) {}
  }
}
