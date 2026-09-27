package dev.simulated_team.simulated.content.blocks.rope.rope_connector;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class RopeConnectorBlock extends Block implements IBE<RopeConnectorBlockEntity> {
    public RopeConnectorBlock(Properties properties) { super(properties); }
    @Override public Class<RopeConnectorBlockEntity> getBlockEntityClass() { return RopeConnectorBlockEntity.class; }
    @Override public BlockEntityType<? extends RopeConnectorBlockEntity> getBlockEntityType() { return null; }
}
