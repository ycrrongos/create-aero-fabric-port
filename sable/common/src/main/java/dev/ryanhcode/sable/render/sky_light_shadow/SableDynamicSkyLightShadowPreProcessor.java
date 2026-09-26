package dev.ryanhcode.sable.render.sky_light_shadow;

import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;

/**
 * Stubbed for 1.21.11 spike.
 */
public class SableDynamicSkyLightShadowPreProcessor implements ShaderPreProcessor {
    public static final String SAMPLER_NAME = "SableShadowSampler";
    public static final String SHADOW_VOLUME_SIZE_UNIFORM = "SableShadowVolumeSize";
    public static final String ENABLE_UNIFORM = "SableShadowsEnabled";
    public static final String SHADOW_ORIGIN_UNIFORM = "SableShadowOrigin";

    @Override
    public void modify(final Context ctx, final GlslTree tree) throws GlslSyntaxException {
        // no-op until RenderPipeline port
    }
}
