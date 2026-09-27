package dev.simulated_team.simulated.content.blocks.redstone_magnet;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class RedstoneMagnetBlock extends Block implements IBE<RedstoneMagnetBlockEntity> {
    public RedstoneMagnetBlock(Properties properties) { super(properties); }
    @Override public Class<RedstoneMagnetBlockEntity> getBlockEntityClass() { return RedstoneMagnetBlockEntity.class; }
    @Override public BlockEntityType<? extends RedstoneMagnetBlockEntity> getBlockEntityType() { return null; }
}
