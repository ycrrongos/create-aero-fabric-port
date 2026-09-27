package dev.simulated_team.simulated.content.blocks.lasers.optical_sensor;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class OpticalSensorBlock extends Block implements IBE<OpticalSensorBlockEntity> {
    public OpticalSensorBlock(Properties properties) { super(properties); }
    @Override public Class<OpticalSensorBlockEntity> getBlockEntityClass() { return OpticalSensorBlockEntity.class; }
    @Override public BlockEntityType<? extends OpticalSensorBlockEntity> getBlockEntityType() { return null; }
}
