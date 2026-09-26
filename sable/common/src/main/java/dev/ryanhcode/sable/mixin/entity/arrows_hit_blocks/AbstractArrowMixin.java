package dev.ryanhcode.sable.mixin.entity.arrows_hit_blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.mixinhelpers.CanFallAtleastHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes the delta movement that arrows get & the direction they face when they hit blocks
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity {

    /**
     * The position of the arrow before it stepped to its hit location.
     * Stepping onto a sub-level block moves the arrow straight into the plot before {@link AbstractArrow#onHitBlock} runs,
     * so this is what the arrow's position was when it hit the block.
     */
    @Unique
    private @Nullable Vec3 sable$preStepPosition = null;

    @Shadow
    protected abstract boolean isInGround();

    public AbstractArrowMixin(final EntityType<?> entityType, final Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "stepMoveAndHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;setPos(Lnet/minecraft/world/phys/Vec3;)V"))
    private void sable$stepToHit(final AbstractArrow instance, final Vec3 pos, final Operation<Void> original, @Local @Nullable final EntityHitResult entityHitResult) {
        this.sable$preStepPosition = instance.position();

        if (entityHitResult != null) {
            // entities inside sub-levels are hit inside their plot, but the arrow stays in the world when hitting them
            original.call(instance, Sable.HELPER.projectOutOfSubLevel(this.level(), pos));
            return;
        }

        original.call(instance, pos);
    }

    /**
     * When the arrow steps into a sub-level, apply the effects of the blocks it passed through in the sub-level's local space
     */
    @WrapOperation(method = "stepMoveAndHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;applyEffectsFromBlocks(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)V"))
    private void sable$applyEffectsFromBlocks(final AbstractArrow instance, final Vec3 from, final Vec3 to, final Operation<Void> original) {
        final SubLevel subLevel = Sable.HELPER.getContaining(this.level(), to);

        if (subLevel != null && Sable.HELPER.getContaining(this.level(), from) != subLevel) {
            original.call(instance, subLevel.logicalPose().transformPositionInverse(from), to);
            return;
        }

        original.call(instance, from, to);
    }

    @Inject(method = "stepMoveAndHit", at = @At("RETURN"))
    private void sable$forgetPreStepPosition(final BlockHitResult hitResult, final CallbackInfo ci) {
        this.sable$preStepPosition = null;
    }

    @WrapOperation(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;setPos(Lnet/minecraft/world/phys/Vec3;)V"))
    private void sable$setPos(final AbstractArrow instance,
                              final Vec3 pos,
                              final Operation<Void> original,
                              @Local(argsOnly = true) final BlockHitResult blockHitResult) {
        final SubLevel subLevel = Sable.HELPER.getContaining(this.level(), blockHitResult.getLocation());

        if (subLevel == null) {
            original.call(instance, pos);
            return;
        }

        final Vec3 preHitPosition = this.sable$preStepPosition != null ? this.sable$preStepPosition : this.position();
        this.sable$preStepPosition = null;

        final Vec3 localPosition = subLevel.logicalPose().transformPositionInverse(preHitPosition);
        final Vec3 difference = blockHitResult.getLocation().subtract(localPosition);

        if (!this.level().isClientSide() && !this.isInGround()) {
            final Vec3 localImpulse = subLevel.logicalPose().transformNormalInverse(this.getDeltaMovement());
            RigidBodyHandle.of((ServerSubLevel) subLevel).applyImpulseAtPoint(localPosition, localImpulse);
        }

        // back the arrow out of the block it hit like vanilla does, but in the local space of the sub-level
        final Vec3 nudge = new Vec3(Math.signum(difference.x), Math.signum(difference.y), Math.signum(difference.z)).scale(0.05F);
        original.call(instance, blockHitResult.getLocation().subtract(nudge));

        final double d = difference.horizontalDistance();
        this.setXRot((float) (Mth.atan2(difference.y, d) * 57.2957763671875));
        this.setYRot((float) (Mth.atan2(difference.x, difference.z) * 57.2957763671875));

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Inject(method = "startFalling", at = @At("TAIL"))
    private void sable$startFalling(final CallbackInfo ci) {
        final SubLevel subLevel = Sable.HELPER.getContaining(this);

        if (subLevel != null) {
            EntitySubLevelUtil.kickEntity(subLevel, this);
        }
    }

    @Redirect(method = "shouldFall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/phys/AABB;)Z"))
    private boolean sable$noCollision(final Level level, final AABB aabb) {
        final boolean original = level.noCollision(this, aabb);

        if (!original) return false;

        return CanFallAtleastHelper.canFallAtleastWithSubLevels(level, aabb) == null;
    }
}
