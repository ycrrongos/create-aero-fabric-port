package dev.simulated_team.simulated.content.blocks.absorber;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class AbsorberBlock extends Block implements IBE<AbsorberBlockEntity> {
    public AbsorberBlock(Properties properties) { super(properties); }
    @Override public Class<AbsorberBlockEntity> getBlockEntityClass() { return AbsorberBlockEntity.class; }
    @Override public BlockEntityType<? extends AbsorberBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.ABSORBER.get(); }
}
