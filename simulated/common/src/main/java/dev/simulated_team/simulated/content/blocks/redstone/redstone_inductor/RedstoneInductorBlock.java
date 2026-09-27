package dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class RedstoneInductorBlock extends Block implements IBE<RedstoneInductorBlockEntity> {
    public RedstoneInductorBlock(Properties properties) { super(properties); }
    @Override public Class<RedstoneInductorBlockEntity> getBlockEntityClass() { return RedstoneInductorBlockEntity.class; }
    @Override public BlockEntityType<? extends RedstoneInductorBlockEntity> getBlockEntityType() { return null; }
}
