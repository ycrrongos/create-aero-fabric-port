package dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class ModulatingLinkedReceiverBlock extends Block implements IBE<ModulatingLinkedReceiverBlockEntity> {
    public ModulatingLinkedReceiverBlock(Properties properties) { super(properties); }
    @Override public Class<ModulatingLinkedReceiverBlockEntity> getBlockEntityClass() { return ModulatingLinkedReceiverBlockEntity.class; }
    @Override public BlockEntityType<? extends ModulatingLinkedReceiverBlockEntity> getBlockEntityType() { return null; }
}
