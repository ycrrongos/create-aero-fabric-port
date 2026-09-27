package dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
public class RedstoneInductorBlockEntity extends SmartBlockEntity {
    public RedstoneInductorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
}
