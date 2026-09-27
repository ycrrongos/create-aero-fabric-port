package dev.simulated_team.simulated.content.blocks.absorber;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
public class AbsorberBlockEntity extends SmartBlockEntity {
    public AbsorberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
}
