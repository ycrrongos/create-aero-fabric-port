package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter;

import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LinkedTypewriterBlock extends Block implements IBE<LinkedTypewriterBlockEntity> {
    public LinkedTypewriterBlock(Properties properties) {
        super(properties);
    }
    @Override
    public Class<LinkedTypewriterBlockEntity> getBlockEntityClass() {
        return LinkedTypewriterBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends LinkedTypewriterBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.LINKED_TYPEWRITER.get();
    }
}
