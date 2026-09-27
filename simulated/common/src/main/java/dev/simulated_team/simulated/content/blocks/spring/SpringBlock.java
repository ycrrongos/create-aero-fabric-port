package dev.simulated_team.simulated.content.blocks.spring;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
public class SpringBlock extends Block implements IBE<SpringBlockEntity> {
    public static final EnumProperty<Size> SIZE = EnumProperty.create("size", Size.class);
    public SpringBlock(Properties properties) { super(properties); }
    public enum Size implements StringRepresentable {
        SMALL, MEDIUM, LARGE;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }
    @Override public Class<SpringBlockEntity> getBlockEntityClass() { return SpringBlockEntity.class; }
    @Override public BlockEntityType<? extends SpringBlockEntity> getBlockEntityType() { return null; }
}
