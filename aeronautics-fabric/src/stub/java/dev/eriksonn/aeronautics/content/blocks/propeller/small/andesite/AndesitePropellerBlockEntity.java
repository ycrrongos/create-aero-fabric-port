package dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import java.util.List;
/** Compile stub — SmartBlockEntity API rewrite deferred. */
public class AndesitePropellerBlockEntity extends SmartBlockEntity {
  public AndesitePropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
  public AndesitePropellerBlockEntity(BlockPos pos, BlockState state) { super(null, pos, state); }
  @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
}
