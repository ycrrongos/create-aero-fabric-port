package dev.simulated_team.simulated.content.blocks.auger_shaft;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import java.util.List;
/** Compile stub — SmartBlockEntity API rewrite deferred. */
public class AugerShaftBlockEntity extends SmartBlockEntity {
  public AugerShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
  public AugerShaftBlockEntity(BlockPos pos, BlockState state) { super(null, pos, state); }
  @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
}
