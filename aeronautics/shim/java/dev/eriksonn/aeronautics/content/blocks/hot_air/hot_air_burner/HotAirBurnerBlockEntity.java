package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
public class HotAirBurnerBlockEntity extends SmartBlockEntity {
    public HotAirBurnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
    public int getSignalStrength() { return 0; }
    public void updateSignal() {}
}
