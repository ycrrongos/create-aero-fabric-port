package dev.eriksonn.aeronautics.content.blocks.propeller.small;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
public class BasePropellerBlockEntity extends KineticBlockEntity {
    public BasePropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
    public float getScaledThrust() { return 0f; }
    public float getAirflowTickSpeed() { return 0f; }
}
