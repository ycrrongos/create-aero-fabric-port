package dev.simulated_team.simulated.content.blocks.nav_table;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class NavTableBlock extends Block implements IBE<NavTableBlockEntity> {
    public NavTableBlock(Properties properties) { super(properties); }
    @Override public Class<NavTableBlockEntity> getBlockEntityClass() { return NavTableBlockEntity.class; }
    @Override public BlockEntityType<? extends NavTableBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.NAV_TABLE.get(); }
}
