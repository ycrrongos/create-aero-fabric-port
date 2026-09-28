package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class PropellerBearingBlockEntity extends MechanicalBearingBlockEntity {
    private boolean assembleNextTick;

    public PropellerBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
    }

    public void startDisassemblySlowdown() {}

    public void setAssembleNextTick(boolean v) {
        this.assembleNextTick = v;
        setChanged();
    }

    public void forceTilt(BlockState state) {}

    @Override
    public void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putBoolean("AssembleNextTick", this.assembleNextTick);
    }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        this.assembleNextTick = input.getBooleanOr("AssembleNextTick", false);
    }
}
