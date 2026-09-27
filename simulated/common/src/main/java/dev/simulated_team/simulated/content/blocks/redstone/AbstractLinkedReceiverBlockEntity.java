package dev.simulated_team.simulated.content.blocks.redstone;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
public class AbstractLinkedReceiverBlockEntity extends SmartBlockEntity {
    public AbstractLinkedReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
}
