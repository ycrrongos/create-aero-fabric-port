package dev.simulated_team.simulated.content.blocks.nameplate;

import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class NameplateBlock extends Block implements IBE<NameplateBlockEntity> {
    public static final EnumProperty<Position> POSITION = EnumProperty.create("position", Position.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public NameplateBlock(Properties properties) { super(properties); }
    public enum Position implements StringRepresentable {
        WALL, CEILING, FLOOR;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }
    @Override public Class<NameplateBlockEntity> getBlockEntityClass() { return NameplateBlockEntity.class; }
    @Override public BlockEntityType<? extends NameplateBlockEntity> getBlockEntityType() { return null; }
}
