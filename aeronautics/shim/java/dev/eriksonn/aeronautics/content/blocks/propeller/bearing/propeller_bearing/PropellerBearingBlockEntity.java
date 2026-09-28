package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.List;
public class PropellerBearingBlockEntity extends SmartBlockEntity {
    private float angle;
    private boolean running;
    public PropellerBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
    public float getAngle() { return angle; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; setChanged(); }
    @Override protected void write(ValueOutput output, boolean clientPacket) { super.write(output, clientPacket); output.putFloat("Angle", angle); output.putBoolean("Running", running); }
    @Override protected void read(ValueInput input, boolean clientPacket) { super.read(input, clientPacket); angle = input.getFloatOr("Angle", 0f); running = input.getBooleanOr("Running", false); }
}
