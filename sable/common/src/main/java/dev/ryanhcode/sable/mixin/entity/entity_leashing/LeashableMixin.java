package dev.ryanhcode.sable.mixin.entity.entity_leashing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes leash physics take into account sub-levels
 */
@Mixin(Leashable.class)
public interface LeashableMixin {

    /**
     * Take into account sub-levels for the distance between a leashed entity and its holder, which decides if the leash
     * is slack, pulling or snaps
     */
    @WrapOperation(method = "leashDistanceTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"))
    private double sable$leashDistanceTo(final Vec3 holderCenter, final Vec3 leashedCenter, final Operation<Double> original, @Local(argsOnly = true) final Entity holder) {
        return Math.sqrt(Sable.HELPER.distanceSquaredWithSubLevels(holder.level(), holderCenter, leashedCenter));
    }

    /**
     * Take into account sub-levels for the attachment points of the leash on the leashed entity and its holder
     */
    @WrapOperation(method = "computeElasticInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 sable$projectAttachmentPoint(final Vec3 position, final Vec3 offset, final Operation<Vec3> original, @Local(argsOnly = true, ordinal = 0) final Entity leashedEntity) {
        return Sable.HELPER.projectOutOfSubLevel(leashedEntity.level(), original.call(position, offset));
    }

    /**
     * The elastic impulse is computed globally, bring it into the local space of the leashed entity if it's inside a sub-level
     */
    @WrapOperation(method = "checkElasticInteractions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void sable$addElasticImpulse(final Entity leashedEntity, final Vec3 impulse, final Operation<Void> original) {
        final SubLevel leashedSubLevel = Sable.HELPER.getContaining(leashedEntity);

        if (leashedSubLevel != null) {
            original.call(leashedEntity, leashedSubLevel.logicalPose().transformNormalInverse(impulse));
            return;
        }

        original.call(leashedEntity, impulse);
    }
}
