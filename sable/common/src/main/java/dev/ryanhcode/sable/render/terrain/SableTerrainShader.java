package dev.ryanhcode.sable.render.terrain;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * Uniforms Sable injects into the vanilla terrain shader, and helpers to set them on the terrain pipelines.
 * <p>
 * All uniforms are plain (non block) uniforms, so their values are stored per program and persist between draws. Every
 * uniform defaults to 0, which is the vanilla behavior.
 */
public final class SableTerrainShader {

    public static final Identifier TERRAIN_SHADER = Identifier.withDefaultNamespace("core/terrain");

    /**
     * 1 while drawing sub-level sections
     */
    public static final String SUB_LEVEL = "SableSubLevel";
    /**
     * Transforms plot space positions (relative to the sub-level anchor) into camera relative world space
     */
    public static final String SUB_LEVEL_TRANSFORM = "SableSubLevelTransform";
    /**
     * 1 to apply dynamic directional shading from the world space normal
     */
    public static final String NORMAL_LIGHTING = "SableEnableNormalLighting";
    /**
     * How much to dim the sky light, {@code 1 - skyLightScale}
     */
    public static final String SKY_LIGHT_DIM = "SableSkyLightDim";
    /**
     * The brightness of each block face direction in the current level
     */
    public static final String BLOCK_FACE_BRIGHTNESS = "SableBlockFaceBrightness";

    public static final String SHADOW_SAMPLER = "SableShadowSampler";
    public static final String SHADOW_VOLUME_SIZE = "SableShadowVolumeSize";
    public static final String SHADOWS_ENABLED = "SableShadowsEnabled";
    public static final String SHADOW_ORIGIN = "SableShadowOrigin";

    public static final String WATER_OCCLUSION_CLOSE_SAMPLER = "SableCloseSampler";
    public static final String WATER_OCCLUSION_FAR_SAMPLER = "SableFarSampler";
    public static final String WATER_OCCLUSION_ENABLED = "SableWaterOcclusionEnabled";

    /**
     * Texture units Sable binds its extra terrain samplers to. Vanilla terrain pipelines only use the first few units.
     */
    public static final int SHADOW_TEXTURE_UNIT = 13;
    public static final int WATER_OCCLUSION_CLOSE_TEXTURE_UNIT = 14;
    public static final int WATER_OCCLUSION_FAR_TEXTURE_UNIT = 15;

    /**
     * Every vanilla pipeline using the terrain shader
     */
    public static final List<RenderPipeline> PIPELINES = List.of(
            RenderPipelines.SOLID_TERRAIN,
            RenderPipelines.CUTOUT_TERRAIN,
            RenderPipelines.TRANSLUCENT_TERRAIN,
            RenderPipelines.TRIPWIRE_TERRAIN,
            RenderPipelines.WIREFRAME
    );

    private static final float[] BLOCK_FACE_BRIGHTNESS_VALUES = new float[6];

    private SableTerrainShader() {
    }

    private static ShaderUniformAccess uniform(final RenderPipeline pipeline, final String name) {
        return VeilRenderSystem.getVanillaUniform(pipeline, name);
    }

    /**
     * Updates the per-frame uniforms of all terrain pipelines.
     */
    @ApiStatus.Internal
    public static void updateFrame(final ClientLevel level) {
        for (final Direction direction : Direction.values()) {
            BLOCK_FACE_BRIGHTNESS_VALUES[direction.ordinal()] = level.getShade(direction, true);
        }

        for (final RenderPipeline pipeline : PIPELINES) {
            uniform(pipeline, BLOCK_FACE_BRIGHTNESS).setFloats(BLOCK_FACE_BRIGHTNESS_VALUES);
        }
    }

    /**
     * Sets up a terrain pipeline to draw the sections of a sub-level.
     *
     * @param pipeline      The terrain pipeline
     * @param transform     The transform from anchor relative plot space into camera relative world space
     * @param skyLightScale The scale applied to the sky light of the sub-level, in [0, 1]
     */
    public static void setupSubLevel(final RenderPipeline pipeline, final Matrix4fc transform, final float skyLightScale) {
        uniform(pipeline, SUB_LEVEL).setFloat(1.0F);
        uniform(pipeline, SUB_LEVEL_TRANSFORM).setMatrix(transform);
        uniform(pipeline, SKY_LIGHT_DIM).setFloat(1.0F - skyLightScale);
        // Sub-levels never shadow themselves
        uniform(pipeline, SHADOWS_ENABLED).setFloat(0.0F);
    }

    /**
     * Enables or disables dynamic directional shading of sub-level geometry.
     */
    public static void setupNormalLighting(final RenderPipeline pipeline, final boolean normalLighting) {
        uniform(pipeline, NORMAL_LIGHTING).setFloat(normalLighting ? 1.0F : 0.0F);
    }

    /**
     * Restores a terrain pipeline to draw the world.
     *
     * @param pipeline       The terrain pipeline
     * @param shadowsEnabled Whether sub-level sky light shadows should be drawn onto the world
     */
    public static void setupWorld(final RenderPipeline pipeline, final boolean shadowsEnabled) {
        uniform(pipeline, SUB_LEVEL).setFloat(0.0F);
        uniform(pipeline, SKY_LIGHT_DIM).setFloat(0.0F);
        uniform(pipeline, SHADOWS_ENABLED).setFloat(shadowsEnabled ? 1.0F : 0.0F);
    }
}
