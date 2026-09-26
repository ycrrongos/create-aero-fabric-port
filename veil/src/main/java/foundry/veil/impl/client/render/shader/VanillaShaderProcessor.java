package foundry.veil.impl.client.render.shader;

import com.mojang.blaze3d.shaders.ShaderType;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.ShaderStage;
import foundry.veil.api.client.render.shader.processor.ShaderImporter;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GLCapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies registered Veil pre-processors to vanilla core shaders right before the 1.21.11 GL backend compiles them.
 */
@ApiStatus.Internal
public final class VanillaShaderProcessor {

    private static final ThreadLocal<ShaderKey> CURRENT = new ThreadLocal<>();

    private VanillaShaderProcessor() {
    }

    public static void setCurrent(Identifier id, ShaderType type) {
        CURRENT.set(new ShaderKey(id, type));
    }

    /**
     * Processes the final source of a vanilla shader.
     *
     * @param source  The source, with vanilla defines already injected
     * @param defines The defines the vanilla pipeline uses
     * @return The modified source
     */
    public static String process(String source, ShaderDefines defines) {
        ShaderKey key = CURRENT.get();
        CURRENT.remove();
        if (key == null || !VeilRenderSystem.isRendererAvailable()) {
            return source;
        }

        VeilShaderPreProcessorList processors = VeilRenderSystem.renderer().getShaderPreProcessors();
        if (processors.isEmpty()) {
            return source;
        }

        ShaderStage stage = ShaderStage.fromVanilla(key.type());
        ShaderImporter importer = VeilRenderSystem.renderer().getShaderManager().createImporter();
        ShaderPreProcessor.MinecraftContext context = new ShaderPreProcessor.MinecraftContext() {
            @Override
            public String shaderInstance() {
                return key.id().getPath();
            }

            @Override
            public Identifier shaderId() {
                return key.id();
            }

            @Override
            public ShaderDefines defines() {
                return defines;
            }

            @Override
            public Identifier name() {
                return key.id();
            }

            @Override
            public boolean isSourceFile() {
                return true;
            }

            @Override
            public ShaderStage type() {
                return stage;
            }

            @Override
            public GLCapabilities glCapabilities() {
                return VeilRenderSystem.glCapabilities();
            }

            @Override
            public ShaderImporter shaderImporter() {
                return importer;
            }

            @Override
            public Map<String, String> macros() {
                return defines.values();
            }
        };

        try {
            GlslTree tree = GlslParser.preprocessParse(source, new HashMap<>());
            String before = tree.toSourceString();
            processors.processor().modify(context, tree);
            String after = tree.toSourceString();
            return before.equals(after) ? source : after;
        } catch (Exception e) {
            Veil.LOGGER.error("Failed to apply Veil pre-processors to vanilla shader {} ({})", key.id(), key.type(), e);
            return source;
        }
    }

    private record ShaderKey(Identifier id, ShaderType type) {
    }
}
