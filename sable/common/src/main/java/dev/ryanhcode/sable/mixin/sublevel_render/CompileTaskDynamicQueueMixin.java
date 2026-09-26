package dev.ryanhcode.sable.mixin.sublevel_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.sublevel.render.vanilla.SubLevelSectionCameras;
import net.minecraft.client.renderer.chunk.CompileTaskDynamicQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Prioritizes sub-level section compilation by the distance to the camera in plot space.
 */
@Mixin(CompileTaskDynamicQueue.class)
public class CompileTaskDynamicQueueMixin {

    @WrapOperation(method = "poll", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distToCenterSqr(Lnet/minecraft/core/Position;)D"))
    private double sable$subLevelDistance(final BlockPos origin, final Position cameraPosition, final Operation<Double> original) {
        final Vec3 localCamera = SubLevelSectionCameras.getLocalCamera(SectionPos.blockToSectionCoord(origin.getX()), SectionPos.blockToSectionCoord(origin.getZ()));
        return original.call(origin, localCamera != null ? localCamera : cameraPosition);
    }
}
