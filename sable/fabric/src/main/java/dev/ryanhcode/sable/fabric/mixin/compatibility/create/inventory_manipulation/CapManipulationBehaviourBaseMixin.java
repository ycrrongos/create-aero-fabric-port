package dev.ryanhcode.sable.fabric.mixin.compatibility.create.inventory_manipulation;

import com.google.common.base.Predicate;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets Create's inventory manipulation behaviours (funnels, brass tunnels, ...) find inventories on sub-levels.
 * <p>
 * Create Fly replaced the NeoForge {@code Level#getCapability} lookup with its own
 * {@code CapManipulationBehaviourBase#getCapability(Level, BlockPos, BlockEntity, Direction)} lookup.
 */
@Mixin(CapManipulationBehaviourBase.class)
public abstract class CapManipulationBehaviourBaseMixin<T> {

    @Shadow protected Predicate<BlockEntity> filter;

    @Shadow protected boolean bypassSided;

    @Unique
    private BlockPos sable$caughtPos;

    @Shadow
    protected abstract T getCapability(Level world, BlockPos pos, BlockEntity blockEntity, Direction side);

    @Redirect(method = "findNewCapability", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity sable$findNewCapOnSubLevel(final Level level, final BlockPos blockPos) {
        final ActiveSableCompanion helper = Sable.HELPER;
        return helper.runIncludingSubLevels(level, blockPos.getCenter(), true, helper.getContaining(level, blockPos), (subLevel, internalPos) -> {
            final BlockEntity caughtBE = level.getBlockEntity(internalPos);
            if (this.filter.apply(caughtBE)) {
                this.sable$caughtPos = internalPos;
                return caughtBE;
            }

            return null;
        });
    }

    @Redirect(method = "findNewCapability", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/foundation/blockEntity/behaviour/inventory/CapManipulationBehaviourBase;getCapability(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/core/Direction;)Ljava/lang/Object;"))
    public T sable$redirectPos(final CapManipulationBehaviourBase<?, ?> instance, final Level world, final BlockPos pos, final BlockEntity blockEntity, final Direction side) {
        return this.getCapability(world, this.sable$caughtPos, blockEntity, side);
    }
}
