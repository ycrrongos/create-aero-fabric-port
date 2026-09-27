package dev.simulated_team.simulated.content.blocks.steering_wheel;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class SteeringWheelBlock extends Block implements IBE<SteeringWheelBlockEntity> {
    public SteeringWheelBlock(Properties properties) { super(properties); }
    @Override public Class<SteeringWheelBlockEntity> getBlockEntityClass() { return SteeringWheelBlockEntity.class; }
    @Override public BlockEntityType<? extends SteeringWheelBlockEntity> getBlockEntityType() { return null; }
}
