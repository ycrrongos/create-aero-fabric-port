package dev.eriksonn.aeronautics.content.blocks.hot_air.steam_vent;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class SteamVentBlockEntity extends SmartBlockEntity {
    public int rawSignalStrength;

    public SteamVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}

    public void getAndCacheTank() {}

    public boolean updateRawSignal() {
        return false;
    }

    public void signalSync() {}

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putInt("RawSignal", this.rawSignalStrength);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        this.rawSignalStrength = input.getIntOr("RawSignal", 0);
    }
}
