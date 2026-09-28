package dev.simulated_team.simulated.content.blocks.lasers.laser_pointer;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class LaserPointerBlock extends Block implements IBE<LaserPointerBlockEntity> {
    public LaserPointerBlock(Properties properties) { super(properties); }
    @Override public Class<LaserPointerBlockEntity> getBlockEntityClass() { return LaserPointerBlockEntity.class; }
    @Override public BlockEntityType<? extends LaserPointerBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.LASER_POINTER.get(); }
}
