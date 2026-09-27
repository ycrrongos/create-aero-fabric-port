package dev.simulated_team.simulated.content.blocks.docking_connector;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class DockingConnectorBlock extends Block implements IBE<DockingConnectorBlockEntity> {
    public DockingConnectorBlock(Properties properties) { super(properties); }
    @Override public Class<DockingConnectorBlockEntity> getBlockEntityClass() { return DockingConnectorBlockEntity.class; }
    @Override public BlockEntityType<? extends DockingConnectorBlockEntity> getBlockEntityType() { return null; }
}
