package dev.ryanhcode.sable.fabric.mixin.compatibility.create.fluid_handling;

import com.zurrtum.create.client.AllHandle;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes fluid pipe particles spawn when the camera is close to pipes on sub-levels.
 * <p>
 * Create Fly moved {@code PipeConnection#isRenderEntityWithinDistance} to the client-only
 * {@code AllHandle#isRenderEntityWithoutDistance} (with an inverted result), called from
 * {@code AllHandle#spawnPipeParticles}.
 */
@Mixin(AllHandle.class)
public class PipeConnectionMixin {
    @Redirect(method = "isRenderEntityWithoutDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"))
    private static double sable$distanceIncludingSubLevels(final Vec3 instance, final Vec3 vec3) {
        return Math.sqrt(Sable.HELPER.distanceSquaredWithSubLevels(Minecraft.getInstance().level, instance, vec3));
    }
}
