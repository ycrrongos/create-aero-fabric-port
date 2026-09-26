package dev.ryanhcode.sable.render.sky_light_shadow;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.render.terrain.SableTerrainShader;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderContext;
import dev.ryanhcode.sable.sublevel.render.SubLevelSectionDraws;
import dev.ryanhcode.sable.sublevel.render.dispatcher.SubLevelRenderDispatcher;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.AdvancedFboTextureAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL45C;

/**
 * Sky light shadows cast by sub-levels onto the world.
 * <p>
 * Every frame the sub-levels around the camera are drawn into an orthographic top-down depth map. The terrain shader
 * then darkens the sky light of world geometry that is below sub-level geometry in that map.
 */
public class SableSkyLightShadows {

    public static final float SHADOW_VOLUME_SIZE = 256f / 2f;
    private static final float SHADOW_NEAR_PLANE = 0.5F;
    private static final int SHADOW_MAP_RESOLUTION = 1024;

    private static final Matrix4f PROJECTION_MAT = new Matrix4f();
    private static final Matrix4f SHADOW_VIEW_MAT = new Matrix4f().rotationX((float) (Math.PI / 2));
    private static final Vec3[] SHADOW_CAMERA_POSITION = {Vec3.ZERO};

    private static boolean isRenderingShadowMap = false;
    private static boolean isEnabled = false;
    private static boolean hasShadowMap = false;

    private static @Nullable AdvancedFbo shadowFbo;
    private static @Nullable PerspectiveProjectionMatrixBuffer projectionBuffer;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void setIsEnabled(final boolean isEnabled) {
        SableSkyLightShadows.isEnabled = isEnabled;
        if (!isEnabled) {
            free();
        }
    }

    public static boolean renderingShadowMap() {
        return isRenderingShadowMap;
    }

    private static AdvancedFbo getShadowsFramebuffer() {
        if (shadowFbo == null) {
            shadowFbo = AdvancedFbo.withSize(SHADOW_MAP_RESOLUTION, SHADOW_MAP_RESOLUTION)
                    .setName("Sable sub-level shadows")
                    .addColorTextureBuffer()
                    .setDepthTextureBuffer()
                    .build(true);
        }
        return shadowFbo;
    }

    /**
     * Renders the top-down depth map of all sub-levels near the camera.
     *
     * @param level        The level being rendered
     * @param camera       The camera position
     * @param partialTicks The partial tick
     */
    public static void renderShadowMap(final ClientLevel level, final Vec3 camera, final float partialTicks) {
        hasShadowMap = false;
        if (!isEnabled || VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            return;
        }

        final AdvancedFbo fbo = getShadowsFramebuffer();
        final AdvancedFboTextureAttachment color = fbo.getColorTextureAttachment(0);
        final AdvancedFboTextureAttachment depth = fbo.getDepthTextureAttachment();
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(color.getTexture(), 0xFFFFFFFF, depth.getTexture(), 1.0);

        final Vec3 shadowCamera = new Vec3(Math.floor(camera.x), camera.y + SHADOW_VOLUME_SIZE / 2.0f, Math.floor(camera.z));
        SHADOW_CAMERA_POSITION[0] = shadowCamera;

        PROJECTION_MAT.setOrtho(-SHADOW_VOLUME_SIZE, SHADOW_VOLUME_SIZE, -SHADOW_VOLUME_SIZE, SHADOW_VOLUME_SIZE, SHADOW_NEAR_PLANE, SHADOW_VOLUME_SIZE);
        if (projectionBuffer == null) {
            projectionBuffer = new PerspectiveProjectionMatrixBuffer("Sable sub-level shadows");
        }

        final GpuBufferSlice oldProjection = RenderSystem.getProjectionMatrixBuffer();
        final ProjectionType oldProjectionType = RenderSystem.getProjectionType();
        RenderSystem.setProjectionMatrix(projectionBuffer.getBuffer(PROJECTION_MAT), ProjectionType.ORTHOGRAPHIC);

        isRenderingShadowMap = true;
        VeilLevelPerspectiveRenderer.beginPerspective();
        try {
            final ClientSubLevelContainer container = SubLevelContainer.getContainer(level);
            if (container == null) {
                return;
            }

            final Iterable<ClientSubLevel> subLevels = container.getAllSubLevels();
            final SubLevelRenderContext context = new SubLevelRenderContext(SHADOW_VIEW_MAT, shadowCamera.x, shadowCamera.y, shadowCamera.z, partialTicks,
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST), false, false);

            final SubLevelRenderDispatcher dispatcher = SubLevelRenderDispatcher.get();
            final SubLevelSectionDraws draws = dispatcher.prepareSections(subLevels, context);
            dispatcher.renderSectionLayer(draws, ChunkSectionLayer.SOLID, color.getTextureView(), depth.getTextureView());
            dispatcher.renderSectionLayer(draws, ChunkSectionLayer.CUTOUT, color.getTextureView(), depth.getTextureView());
            hasShadowMap = true;
        } finally {
            VeilLevelPerspectiveRenderer.endPerspective();
            isRenderingShadowMap = false;
            RenderSystem.setProjectionMatrix(oldProjection, oldProjectionType);
        }
    }

    /**
     * Binds the shadow map to the terrain pipelines, so world geometry can be shadowed.
     *
     * @param camera The camera position of the main level render
     */
    public static void bindShadowMap(final Vec3 camera) {
        if (!isEnabled || !hasShadowMap || shadowFbo == null) {
            return;
        }

        final Vec3 shadowCamera = SHADOW_CAMERA_POSITION[0];
        final GpuTextureView depth = shadowFbo.getDepthTextureAttachment().getTextureView();
        bindTexture(SableTerrainShader.SHADOW_TEXTURE_UNIT, VeilRenderSystem.getTextureId(depth));

        for (final RenderPipeline pipeline : SableTerrainShader.PIPELINES) {
            VeilRenderSystem.getVanillaUniform(pipeline, SableTerrainShader.SHADOW_SAMPLER).setInt(SableTerrainShader.SHADOW_TEXTURE_UNIT);
            VeilRenderSystem.getVanillaUniform(pipeline, SableTerrainShader.SHADOW_VOLUME_SIZE).setFloat(SHADOW_VOLUME_SIZE);
            VeilRenderSystem.getVanillaUniform(pipeline, SableTerrainShader.SHADOW_ORIGIN).setVector(
                    (float) (shadowCamera.x - camera.x),
                    (float) (shadowCamera.y - camera.y),
                    (float) (shadowCamera.z - camera.z));
        }
    }

    /**
     * Binds a texture to a texture unit outside the range vanilla tracks, without disturbing the vanilla state caches.
     */
    public static void bindTexture(final int unit, final int texture) {
        if (VeilRenderSystem.directStateAccessSupported()) {
            GL45C.glBindTextureUnit(unit, texture);
        } else {
            final int activeTexture = GL11C.glGetInteger(GL13C.GL_ACTIVE_TEXTURE);
            GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + unit);
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture);
            GL13C.glActiveTexture(activeTexture);
        }
        GL33C.glBindSampler(unit, 0);
    }

    public static void free() {
        hasShadowMap = false;
        if (shadowFbo != null) {
            shadowFbo.free();
            shadowFbo = null;
        }
        if (projectionBuffer != null) {
            projectionBuffer.close();
            projectionBuffer = null;
        }
    }

    private SableSkyLightShadows() {
    }
}
