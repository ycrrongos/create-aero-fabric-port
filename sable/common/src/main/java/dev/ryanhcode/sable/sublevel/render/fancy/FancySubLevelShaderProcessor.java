package dev.ryanhcode.sable.sublevel.render.fancy;

import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;

import java.io.IOException;

/**
 * Stubbed for the 1.21.11 spike — fancy mesh path is excluded until RenderPipeline port.
 */
public class FancySubLevelShaderProcessor implements ShaderPreProcessor {

    public static final String BUFFER_SIZE = "SABLE_TEXTURE_CACHE_SIZE";

    @Override
    public void modify(final Context ctx, final GlslTree tree) throws IOException, GlslSyntaxException, LexerException {
        // no-op
    }
}
