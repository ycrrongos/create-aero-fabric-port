package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
public class GyroscopicPropellerBearingBlockEntity extends MechanicalBearingBlockEntity {
    public GyroscopicPropellerBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) { super.addBehaviours(behaviours); }
    public void startDisassemblySlowdown() {}
    public void setAssembleNextTick(boolean v) {}
    public void forceTilt(BlockState state) {}
}
