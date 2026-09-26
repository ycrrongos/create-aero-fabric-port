package dev.ryanhcode.sable.mixin.entity.entity_leashing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes the leash render state take into account sub-levels the leashed entity and its holder are in
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 sable$getRopeHoldPosition(final Entity instance, final float f, final Operation<Vec3> original, @Local(argsOnly = true) final Entity leashedEntity) {
        return sable$toLeashedEntitySpace(leashedEntity, original.call(instance, f));
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 sable$getEyePosition(final Entity instance, final float f, final Operation<Vec3> original, @Local(argsOnly = true) final Entity leashedEntity) {
        return sable$toLeashedEntitySpace(leashedEntity, original.call(instance, f));
    }

    /**
     * The position of the holder of a quad leash
     */
    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPosition(F)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
    private Vec3 sable$getQuadLeashHolderPosition(final Entity instance, final float f, final Operation<Vec3> original, @Local(argsOnly = true) final Entity leashedEntity) {
        return sable$toLeashedEntitySpace(leashedEntity, original.call(instance, f));
    }

    /**
     * Moves a position out of the sub-level it is in, and into the sub-level the leashed entity is in
     */
    @Unique
    private static Vec3 sable$toLeashedEntitySpace(final Entity leashedEntity, final Vec3 position) {
        final ActiveSableCompanion helper = Sable.HELPER;
        final SubLevel leashedSubLevel = helper.getContaining(leashedEntity);

        final Vector3d transformedPosition = JOMLConversion.toJOML(position);
        final SubLevel holdingSubLevel = helper.getContaining(leashedEntity.level(), transformedPosition);

        if (holdingSubLevel != null) {
            holdingSubLevel.logicalPose().transformPosition(transformedPosition);
        }

        if (leashedSubLevel != null) {
            leashedSubLevel.logicalPose().transformPositionInverse(transformedPosition);
        }

        return JOMLConversion.toMojang(transformedPosition);
    }
}
