package dev.simulated_team.simulated.content.blocks.velocity_sensor;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class VelocitySensorBlock extends Block implements IBE<VelocitySensorBlockEntity> {
    public VelocitySensorBlock(Properties properties) { super(properties); }
    @Override public Class<VelocitySensorBlockEntity> getBlockEntityClass() { return VelocitySensorBlockEntity.class; }
    @Override public BlockEntityType<? extends VelocitySensorBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.VELOCITY_SENSOR.get(); }
}
