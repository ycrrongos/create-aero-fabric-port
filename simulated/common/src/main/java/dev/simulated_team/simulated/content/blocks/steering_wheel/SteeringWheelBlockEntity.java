package dev.simulated_team.simulated.content.blocks.steering_wheel;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

/** Temporary compile stub; full logic backed up under /tmp/be-bak. */
public class SteeringWheelBlockEntity extends SmartBlockEntity {
    public SteeringWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
}
