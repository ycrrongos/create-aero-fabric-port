package dev.ryanhcode.sable.mixin.fluids_on_sub_levels;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes fluids refuse to flow off the edge of sub-levels
 * <p>
 * In 1.21.11 {@code FlowingFluid#canSpreadTo} was removed. It used to be the final gate before
 * {@code spreadTo} for both the downwards spread in {@code spread} and every horizontal spread in
 * {@code spreadToSides}; the same two gates are recreated here.
 */
@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {

    /**
     * Downwards spread: the last condition before spreading down is {@code canHoldSpecificFluid}. Failing it makes
     * vanilla fall through to the sideways spread, exactly like a failing {@code canSpreadTo} did before.
     */
    @WrapOperation(method = "spread(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;canHoldSpecificFluid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z"))
    private boolean sable$canSpreadDown(final BlockGetter pLevel, final BlockPos pToPos, final BlockState pToBlockState, final Fluid pFluid, final Operation<Boolean> original) {
        return original.call(pLevel, pToPos, pToBlockState, pFluid) && sable$canSpreadTo(pLevel, pToPos);
    }

    /**
     * Sideways spread: skip spreading into a direction that would flow off the edge of a sub-level.
     */
    @WrapWithCondition(method = "spreadToSides", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;spreadTo(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)V"))
    private boolean sable$canSpreadToSide(final FlowingFluid instance, final LevelAccessor pLevel, final BlockPos pToPos, final BlockState pToBlockState, final Direction pDirection, final FluidState pFluidState) {
        return sable$canSpreadTo(pLevel, pToPos);
    }

    @Unique
    private static boolean sable$canSpreadTo(final BlockGetter pLevel, final BlockPos pToPos) {
        if (!(pLevel instanceof final Level level)) {
            return true;
        }

        final SubLevel subLevel = Sable.HELPER.getContaining(level, Vec3.atCenterOf(pToPos));

        if (subLevel != null) {
            BlockPos mut = pToPos;
            boolean ableToFlow = false;

            while (subLevel.getPlot().getBoundingBox().contains(mut.getX(), mut.getY(), mut.getZ())) {
                mut = mut.below();

                if (mut.getY() < 0 || !pLevel.getBlockState(mut).isAir()) {
                    ableToFlow = true;
                    break;
                }
            }

            return ableToFlow;
        }

        return true;
    }

}
