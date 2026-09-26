package dev.ryanhcode.sable.render.dynamic_shade;

import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;

import java.io.IOException;

/**
 * Dynamic shading shader preprocessor stub (RenderType.name / chunk layers need RenderPipeline port).
 */
public class SableDynamicDirectionalShadingPreProcessor implements ShaderPreProcessor {

    @Override
    public void modify(final Context ctx, final GlslTree tree) throws GlslSyntaxException, IOException, LexerException {
        // no-op for 1.21.11 compile spike
    }
}
