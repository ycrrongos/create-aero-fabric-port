package dev.simulated_team.simulated.content.blocks.torsion_spring;

import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class TorsionSpringBlock extends Block implements IBE<TorsionSpringBlockEntity> {
    public TorsionSpringBlock(Properties properties) { super(properties); }
    @Override public Class<TorsionSpringBlockEntity> getBlockEntityClass() { return TorsionSpringBlockEntity.class; }
    @Override public BlockEntityType<? extends TorsionSpringBlockEntity> getBlockEntityType() { return SimBlockEntityTypes.TORSION_SPRING.get(); }
}
