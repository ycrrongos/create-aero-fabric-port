package dev.ryanhcode.sable.mixin.sublevel_render;

import dev.ryanhcode.sable.sublevel.render.vanilla.SubLevelSectionCameras;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Section compile and sort tasks record the camera position the translucent geometry was sorted for. For sub-level
 * sections this has to be the camera in plot space, otherwise they would never be considered sorted.
 */
@Mixin(TranslucencyPointOfView.class)
public class TranslucencyPointOfViewMixin {

    @ModifyVariable(method = "of", at = @At("HEAD"), argsOnly = true)
    private static Vec3 sable$useLocalCamera(final Vec3 cameraPosition, final Vec3 unused, final long sectionNode) {
        final Vec3 localCamera = SubLevelSectionCameras.getLocalCamera(sectionNode);
        return localCamera != null ? localCamera : cameraPosition;
    }
}
