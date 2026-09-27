package dev.simulated_team.simulated.content.blocks.merging_glue;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class MergingGlueBlock extends Block implements IBE<MergingGlueBlockEntity> {
    public MergingGlueBlock(Properties properties) { super(properties); }
    @Override public Class<MergingGlueBlockEntity> getBlockEntityClass() { return MergingGlueBlockEntity.class; }
    @Override public BlockEntityType<? extends MergingGlueBlockEntity> getBlockEntityType() { return null; }
}
