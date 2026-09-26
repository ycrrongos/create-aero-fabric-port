package dev.ryanhcode.sable.render.water_occlusion;

import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;

/**
 * Stubbed for 1.21.11 spike.
 */
public class SableWaterOcclusionPreProcessor implements ShaderPreProcessor {
    public static final String CLOSE_SAMPLER_NAME = "SableCloseSampler";
    public static final String FAR_SAMPLER_NAME = "SableFarSampler";
    public static final String ENABLE_UNIFORM = "SableWaterOcclusionEnabled";

    @Override
    public void modify(final Context ctx, final GlslTree tree) throws GlslSyntaxException {
        // no-op until RenderPipeline port
    }
}
