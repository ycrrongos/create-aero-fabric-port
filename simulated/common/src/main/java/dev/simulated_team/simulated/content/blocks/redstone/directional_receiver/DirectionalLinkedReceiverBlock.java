package dev.simulated_team.simulated.content.blocks.redstone.directional_receiver;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class DirectionalLinkedReceiverBlock extends Block implements IBE<DirectionalLinkedReceiverBlockEntity> {
    public DirectionalLinkedReceiverBlock(Properties properties) { super(properties); }
    @Override public Class<DirectionalLinkedReceiverBlockEntity> getBlockEntityClass() { return DirectionalLinkedReceiverBlockEntity.class; }
    @Override public BlockEntityType<? extends DirectionalLinkedReceiverBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.DIRECTIONAL_LINKED_RECEIVER.get(); }
}
