package dev.ryanhcode.sable.mixin.interaction_distance;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes interaction distance on entity and block interactions
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(final EntityType<? extends LivingEntity> entityType, final Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract double blockInteractionRange();

    @Inject(method = "isWithinBlockInteractionRange(Lnet/minecraft/core/BlockPos;D)Z", at = @At("HEAD"), cancellable = true)
    private void sable$canInteractWithBlock(final BlockPos pos, final double slop, final CallbackInfoReturnable<Boolean> cir) {
        final SubLevel subLevel = Sable.HELPER.getContaining(this.level(), pos);

        if (subLevel != null) {
            final double rangeWithSlop = this.blockInteractionRange() + slop;
            final Vec3 eyePos = subLevel.logicalPose().transformPositionInverse(this.getEyePosition());

            final boolean closeEnough = (new AABB(pos)).distanceToSqr(eyePos) < rangeWithSlop * rangeWithSlop;

            if (closeEnough) cir.setReturnValue(true);
        }
    }

    @Inject(method = "isWithinEntityInteractionRange(Lnet/minecraft/world/phys/AABB;D)Z", at = @At("HEAD"), cancellable = true)
    private void sable$canInteractWithEntity(final AABB aabb, final double slop, final CallbackInfoReturnable<Boolean> cir) {
        if (this.sable$isWithinSubLevelEntityRange(aabb, slop)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * In 1.21.1 attacks were range checked with {@code canInteractWithEntity} as well. 1.21.11 checks them with
     * {@link Player#isWithinAttackRange(AABB, double)} instead, so the same sub-level aware check is applied there.
     */
    @Inject(method = "isWithinAttackRange(Lnet/minecraft/world/phys/AABB;D)Z", at = @At("HEAD"), cancellable = true)
    private void sable$canAttackEntity(final AABB aabb, final double slop, final CallbackInfoReturnable<Boolean> cir) {
        if (this.sable$isWithinSubLevelEntityRange(aabb, slop)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean sable$isWithinSubLevelEntityRange(final AABB aabb, final double slop) {
        // should bottom center be assumed here?
        final SubLevel subLevel = Sable.HELPER.getContaining(this.level(), aabb.getBottomCenter());

        if (subLevel != null) {
            final double rangeWithSlop = this.blockInteractionRange() + slop;
            final Vec3 eyePos = subLevel.logicalPose().transformPositionInverse(this.getEyePosition());

            return aabb.distanceToSqr(eyePos) < rangeWithSlop * rangeWithSlop;
        }

        return false;
    }

}
