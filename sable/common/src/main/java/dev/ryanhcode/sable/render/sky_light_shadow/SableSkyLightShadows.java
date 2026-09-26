package dev.ryanhcode.sable.render.sky_light_shadow;

import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4fc;

/**
 * Stubbed for 1.21.11 spike — skylight shadows need RenderPipeline rewrite.
 */
public class SableSkyLightShadows {

    public static final float SHADOW_VOLUME_SIZE = 256f / 2f;

    private static boolean isEnabled = false;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void setIsEnabled(final boolean isEnabled) {
        SableSkyLightShadows.isEnabled = isEnabled;
    }

    public static void renderShadowMap(final VeilRenderLevelStageEvent.Stage stage, final LevelRenderer levelRenderer, final MultiBufferSource.BufferSource bufferSource, final MatrixStack matrixStack, final Matrix4fc frustumMatrix, final Matrix4fc projectionMatrix, final int renderTick, final DeltaTracker deltaTracker, final Camera camera, final Frustum frustum) {
        // no-op
    }

    public static void bindShadowMapTexture(final Object shader) {
        // no-op
    }
}
