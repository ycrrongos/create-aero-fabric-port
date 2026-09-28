package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon;

import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph.BalloonLayerGraph;
import dev.ryanhcode.sable.util.LevelAccelerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

/**
 * Client-side balloon; GPU heat region renderer temporarily no-op until GL APIs are ported.
 */
public class ClientBalloon extends Balloon {

    private boolean leaking;

    @ApiStatus.Internal
    public ClientBalloon(final Level level, final LevelAccelerator accelerator, final BlockPos controllerPos, final BalloonLayerGraph graph, final ObjectArrayList<BlockEntityLiftingGasProvider> heaters) {
        super(level, accelerator, controllerPos, graph, heaters);
    }

    @Override
    public boolean shouldSpawnGust(final BlockPos pos) {
        return false;
    }

    @Override
    public void setLeaking() {
        super.setLeaking();
        this.leaking = true;
    }

    @Override
    public boolean isValid() {
        return !this.leaking && !this.heaters.isEmpty();
    }
}
