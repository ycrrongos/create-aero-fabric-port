package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import java.util.List;
/** Compile stub — SmartBlockEntity API rewrite deferred. */
public class PropellerBearingBlockEntity extends SmartBlockEntity {
  public PropellerBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
  public PropellerBearingBlockEntity(BlockPos pos, BlockState state) { super(null, pos, state); }
  @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
}
