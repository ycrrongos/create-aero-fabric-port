package dev.ryanhcode.sable.fabric.mixin.compatibility.create.ejector;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EjectorBlock.class)
public class EjectorBlockMixin {

    /**
     * Makes ejectors use the standing on position of entities instead of their block position for launching them, as
     * the on position of entities will be overwritten by Sable to be inside of the plot of a sub-level an entity is
     * resting on. Upstream Create already used {@link Entity#getOnPosLegacy()} here, while Create Fly switched to
     * {@link Entity#blockPosition()}, which would never find ejectors on sub-levels.
     */
    @Redirect(method = "updateEntityMovementAfterFallOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;blockPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos sable$updateEntityAfterFallOn(final Entity instance) {
        return instance.getOnPosLegacy();
    }

    @Redirect(method = "updateEntityMovementAfterFallOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"))
    public double distanceTo(final Vec3 instance, final Vec3 vec, @Local(argsOnly = true) final Entity entity) {
        return Math.sqrt(Sable.HELPER.distanceSquaredWithSubLevels(entity.level(), instance, vec));
    }

    @Redirect(method = "updateEntityMovementAfterFallOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 setPos(final Vec3 instance, final Vec3 vec, @Local(argsOnly = true) final Entity entity) {
        final Vector3d projected = Sable.HELPER.projectOutOfSubLevel(entity.level(), JOMLConversion.toJOML(instance)).add(vec.x, vec.y, vec.z);
        return JOMLConversion.toMojang(projected);
    }
}
