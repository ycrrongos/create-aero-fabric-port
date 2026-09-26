package foundry.veil.api.client.render.shader.processor;

import io.github.ocelot.glslprocessor.api.node.GlslTree;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.Collection;

/**
 * Loads shader include files as parsed trees.
 */
public interface ShaderImporter {

    GlslTree loadImport(ShaderPreProcessor.Context context, Identifier name, boolean force) throws IOException;

    Collection<Identifier> addedImports();
}
