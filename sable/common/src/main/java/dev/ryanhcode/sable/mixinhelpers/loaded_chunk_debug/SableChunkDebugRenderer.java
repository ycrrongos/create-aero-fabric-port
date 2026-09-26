package dev.ryanhcode.sable.mixinhelpers.loaded_chunk_debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.ApiStatus;

/**
 * Debug chunk border overlay stub (RenderType.debugLineStrip API changed in 1.21.11).
 */
@ApiStatus.Internal
public class SableChunkDebugRenderer {

    public static void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final double camX, final double camY, final double camZ) {
        // no-op until debug line RenderType is ported
    }
}
