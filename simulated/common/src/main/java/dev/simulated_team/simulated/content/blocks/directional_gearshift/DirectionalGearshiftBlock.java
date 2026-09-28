package dev.simulated_team.simulated.content.blocks.directional_gearshift;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class DirectionalGearshiftBlock extends Block implements IBE<DirectionalGearshiftBlockEntity> {
    public DirectionalGearshiftBlock(Properties properties) { super(properties); }
    @Override public Class<DirectionalGearshiftBlockEntity> getBlockEntityClass() { return DirectionalGearshiftBlockEntity.class; }
    @Override public BlockEntityType<? extends DirectionalGearshiftBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.DIRECTIONAL_GEARSHIFT.get(); }
}
