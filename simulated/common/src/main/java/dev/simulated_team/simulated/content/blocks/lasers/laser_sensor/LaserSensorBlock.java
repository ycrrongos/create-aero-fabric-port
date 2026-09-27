package dev.simulated_team.simulated.content.blocks.lasers.laser_sensor;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class LaserSensorBlock extends Block implements IBE<LaserSensorBlockEntity> {
    public LaserSensorBlock(Properties properties) { super(properties); }
    @Override public Class<LaserSensorBlockEntity> getBlockEntityClass() { return LaserSensorBlockEntity.class; }
    @Override public BlockEntityType<? extends LaserSensorBlockEntity> getBlockEntityType() { return null; }
}
