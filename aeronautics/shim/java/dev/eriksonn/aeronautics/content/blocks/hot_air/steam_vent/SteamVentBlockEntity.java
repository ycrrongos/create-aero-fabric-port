package dev.eriksonn.aeronautics.content.blocks.hot_air.steam_vent;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
public class SteamVentBlockEntity extends SmartBlockEntity {
    public int rawSignalStrength;
    public SteamVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}
    public void getAndCacheTank() {}
    public boolean updateRawSignal() { return false; }
    public void signalSync() {}
}
