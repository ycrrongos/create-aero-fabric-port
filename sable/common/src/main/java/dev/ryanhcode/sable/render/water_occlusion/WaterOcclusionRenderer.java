package dev.ryanhcode.sable.render.water_occlusion;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.render.region.SimpleCulledRenderRegion;
import dev.ryanhcode.sable.render.sky_light_shadow.SableSkyLightShadows;
import dev.ryanhcode.sable.render.terrain.SableTerrainShader;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionContainer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL32C;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Manages water occlusion rendering for sub-levels.
 * <p>
 * The water occlusion volumes of sub-levels are drawn into two depth buffers, the closest front faces and the closest
 * back faces. Translucent terrain between the two is discarded, so water doesn't render inside boats and ships.
 */
@ApiStatus.Internal
public class WaterOcclusionRenderer {

    /**
     * Draws occlusion volumes into a depth buffer
     */
    public static final RenderPipeline OCCLUSION_VOLUME_PIPELINE = RenderPipeline.builder()
            .withLocation(Sable.sablePath("pipeline/water_occlusion_volume"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withVertexShader("core/rendertype_water_mask")
            .withFragmentShader("core/rendertype_water_mask")
            .withColorWrite(false)
            .withCull(true)
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .build();

    private final Set<SimpleCulledRenderRegion> regions = new ObjectOpenHashSet<>();
    private @Nullable AdvancedFbo closeBuffer;
    private @Nullable AdvancedFbo farBuffer;
    private boolean active;
    private Level level;

    private static boolean isEnabled = false;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void setIsEnabled(final boolean isEnabled) {
        WaterOcclusionRenderer.isEnabled = isEnabled;
    }

    @Nullable
    @ApiStatus.Internal
    public SimpleCulledRenderRegion addRegion(final Collection<BlockPos> blocks) {
        if (blocks.isEmpty()) {
            return null;
        }

        final SimpleCulledRenderRegion region = new WaterOcclusionRenderRegion(blocks);
        this.regions.add(region);
        return region;
    }

    public void removeRegion(final SimpleCulledRenderRegion region) {
        region.free();
        this.regions.remove(region);
    }

    private void freeFramebuffers() {
        if (this.closeBuffer != null) {
            this.closeBuffer.free();
            this.closeBuffer = null;
        }
        if (this.farBuffer != null) {
            this.farBuffer.free();
            this.farBuffer = null;
        }
    }

    private void updateFramebuffers(final boolean needed) {
        final Minecraft minecraft = Minecraft.getInstance();
        final RenderTarget renderTarget = minecraft.getMainRenderTarget();

        if (!needed) {
            this.freeFramebuffers();
            return;
        }

        if (this.closeBuffer == null || this.farBuffer == null || renderTarget.width != this.closeBuffer.getWidth() || renderTarget.height != this.closeBuffer.getHeight()) {
            this.freeFramebuffers();
            this.closeBuffer = AdvancedFbo.withSize(renderTarget.width, renderTarget.height).setName("Sable water occlusion (close)").addColorTextureBuffer().setDepthTextureBuffer().build(true);
            this.farBuffer = AdvancedFbo.withSize(renderTarget.width, renderTarget.height).setName("Sable water occlusion (far)").addColorTextureBuffer().setDepthTextureBuffer().build(true);
        }
    }

    /**
     * Draws the occlusion volumes. Called before the terrain of the main level is drawn.
     *
     * @param frustumMatrix The view rotation matrix
     * @param camera        The camera position
     */
    public void preRenderTranslucent(final Matrix4fc frustumMatrix, final Vec3 camera) {
        this.active = false;
        if (!isEnabled() || SableSkyLightShadows.renderingShadowMap()) {
            return;
        }

        final WaterOcclusionContainer<?> container = WaterOcclusionContainer.getContainer(this.level);
        final boolean needed = !this.regions.isEmpty() && container != null;

        this.updateFramebuffers(needed);

        if (!needed) {
            return;
        }

        // if we're inside any of the regions, we need to clear the depth to 0
        final boolean cameraOccluded = container.isOccluded(camera);

        GL11C.glEnable(GL32C.GL_DEPTH_CLAMP);
        try {
            this.drawVolumes(this.closeBuffer, cameraOccluded ? 0.0 : 1.0, frustumMatrix, camera, false);
            this.drawVolumes(this.farBuffer, 1.0, frustumMatrix, camera, true);
        } finally {
            GL11C.glDisable(GL32C.GL_DEPTH_CLAMP);
        }

        this.active = true;
    }

    private void drawVolumes(final AdvancedFbo target, final double clearDepth, final Matrix4fc frustumMatrix, final Vec3 camera, final boolean backFaces) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                target.getColorTextureAttachment(0).getTexture(), 0,
                target.getDepthTextureAttachment().getTexture(), clearDepth);

        try (final RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Sable water occlusion volumes",
                target.getColorTextureAttachment(0).getTextureView(), OptionalInt.empty(),
                target.getDepthTextureAttachment().getTextureView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setPipeline(OCCLUSION_VOLUME_PIPELINE);

            for (final SimpleCulledRenderRegion region : this.regions) {
                region.render(renderPass, frustumMatrix, camera, backFaces);
            }
        }
    }

    /**
     * Makes the translucent terrain pipeline discard fragments inside occlusion volumes.
     */
    public void setupTranslucentShader() {
        if (!isEnabled()) {
            return;
        }

        final RenderPipeline pipeline = RenderPipelines.TRANSLUCENT_TERRAIN;
        if (!this.active || this.closeBuffer == null || this.farBuffer == null) {
            VeilRenderSystem.getVanillaUniform(pipeline, SableTerrainShader.WATER_OCCLUSION_ENABLED).setFloat(0.0F);
            return;
        }

        SableSkyLightShadows.bindTexture(SableTerrainShader.WATER_OCCLUSION_CLOSE_TEXTURE_UNIT, VeilRenderSystem.getTextureId(this.closeBuffer.getDepthTextureAttachment().getTextureView()));
        SableSkyLightShadows.bindTexture(SableTerrainShader.WATER_OCCLUSION_FAR_TEXTURE_UNIT, VeilRenderSystem.getTextureId(this.farBuffer.getDepthTextureAttachment().getTextureView()));

        VeilRenderSystem.getVanillaUniform(pipeline, SableTerrainShader.WATER_OCCLUSION_CLOSE_SAMPLER).setInt(SableTerrainShader.WATER_OCCLUSION_CLOSE_TEXTURE_UNIT);
        VeilRenderSystem.getVanillaUniform(pipeline, SableTerrainShader.WATER_OCCLUSION_FAR_SAMPLER).setInt(SableTerrainShader.WATER_OCCLUSION_FAR_TEXTURE_UNIT);
        VeilRenderSystem.getVanillaUniform(pipeline, SableTerrainShader.WATER_OCCLUSION_ENABLED).setFloat(1.0F);
    }

    /**
     * Stops discarding translucent terrain, so sub-levels and other users of the pipeline are unaffected.
     */
    public void clearTranslucentShader() {
        if (!isEnabled()) {
            return;
        }

        VeilRenderSystem.getVanillaUniform(RenderPipelines.TRANSLUCENT_TERRAIN, SableTerrainShader.WATER_OCCLUSION_ENABLED).setFloat(0.0F);
    }

    public void update() {
        final Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level != this.level) {
            this.level = minecraft.level;

            this.regions.forEach(SimpleCulledRenderRegion::free);
            this.regions.clear();
        }
    }

}
