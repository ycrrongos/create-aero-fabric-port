package dev.simulated_team.simulated.content.blocks.swivel_bearing;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class SwivelBearingBlock extends Block implements IBE<SwivelBearingBlockEntity> {
    public SwivelBearingBlock(Properties properties) { super(properties); }
    @Override public Class<SwivelBearingBlockEntity> getBlockEntityClass() { return SwivelBearingBlockEntity.class; }
    @Override public BlockEntityType<? extends SwivelBearingBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.SWIVEL_BEARING.get(); }
}
