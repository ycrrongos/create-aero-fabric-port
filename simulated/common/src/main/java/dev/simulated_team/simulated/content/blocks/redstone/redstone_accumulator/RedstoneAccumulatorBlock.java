package dev.simulated_team.simulated.content.blocks.redstone.redstone_accumulator;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class RedstoneAccumulatorBlock extends Block implements IBE<RedstoneAccumulatorBlockEntity> {
    public RedstoneAccumulatorBlock(Properties properties) { super(properties); }
    @Override public Class<RedstoneAccumulatorBlockEntity> getBlockEntityClass() { return RedstoneAccumulatorBlockEntity.class; }
    @Override public BlockEntityType<? extends RedstoneAccumulatorBlockEntity> getBlockEntityType() { return null; }
}
