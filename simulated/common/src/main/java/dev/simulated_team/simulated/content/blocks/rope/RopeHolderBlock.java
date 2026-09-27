package dev.simulated_team.simulated.content.blocks.rope;

import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

public interface RopeHolderBlock<T extends SmartBlockEntity> extends BlockSubLevelAssemblyListener, IBE<T> {
    RopeStrandHolderBehavior getHolder(T be);

    static <T extends SmartBlockEntity> InteractionResult shearRope(
            final RopeHolderBlock<T> block, final Level level, final BlockPos pos, final ServerPlayer player) {
        return InteractionResult.PASS;
    }
}
