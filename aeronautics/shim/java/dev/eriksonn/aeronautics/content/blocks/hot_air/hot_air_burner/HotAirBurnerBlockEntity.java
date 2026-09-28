package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/** Incremental Create Fly stub — full logic in excluded-wip pending balloon/lifting-gas restore. */
public class HotAirBurnerBlockEntity extends SmartBlockEntity {
    private int heatLevel;
    private int signalStrength;

    public HotAirBurnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}

    public int getSignalStrength() {
        return this.signalStrength;
    }

    public void updateSignal() {
        this.signalStrength = this.heatLevel;
    }

    public int getHeatLevel() {
        return this.heatLevel;
    }

    public void setHeatLevel(int heatLevel) {
        this.heatLevel = Math.max(0, Math.min(15, heatLevel));
        updateSignal();
        setChanged();
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putInt("HeatLevel", this.heatLevel);
        output.putInt("Signal", this.signalStrength);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        this.heatLevel = input.getIntOr("HeatLevel", 0);
        this.signalStrength = input.getIntOr("Signal", 0);
    }
}
