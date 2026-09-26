package foundry.veil.api.client.render.shader.processor;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.ShaderPreDefinitions;
import foundry.veil.api.client.render.shader.ShaderStage;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.GlslRootNode;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.api.node.function.GlslFunctionNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslNewFieldNode;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GLCapabilities;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Modifies shader sources before they are compiled. Pre-processors run both for Veil programs ({@link VeilContext}) and
 * for vanilla core shaders ({@link MinecraftContext}).
 */
@FunctionalInterface
public interface ShaderPreProcessor {

    ShaderPreProcessor NOOP = (ctx, source) -> {
    };

    /**
     * Called once before a batch of shaders is processed.
     */
    default void prepare() {
    }

    /**
     * Modifies the specified shader source.
     *
     * @param ctx  Context for modifying shaders
     * @param tree The parsed shader
     */
    void modify(Context ctx, GlslTree tree) throws IOException, GlslSyntaxException, LexerException;

    static ShaderPreProcessor allOf(ShaderPreProcessor... processors) {
        return allOf(Arrays.asList(processors));
    }

    static ShaderPreProcessor allOf(Collection<ShaderPreProcessor> processors) {
        List<ShaderPreProcessor> list = new ArrayList<>(processors.size());
        for (ShaderPreProcessor processor : processors) {
            if (processor != NOOP) {
                list.add(processor);
            }
        }
        if (list.isEmpty()) {
            return NOOP;
        }
        if (list.size() == 1) {
            return list.getFirst();
        }
        return new ShaderPreProcessor() {
            @Override
            public void prepare() {
                for (ShaderPreProcessor processor : list) {
                    processor.prepare();
                }
            }

            @Override
            public void modify(Context ctx, GlslTree tree) throws IOException, GlslSyntaxException, LexerException {
                for (ShaderPreProcessor processor : list) {
                    processor.modify(ctx, tree);
                }
            }
        };
    }

    sealed interface Context permits MinecraftContext, VeilContext {

        /**
         * @return The name of the shader being processed or <code>null</code> for an unnamed source
         */
        @Nullable
        Identifier name();

        /**
         * @return Whether the processed source is a full shader stage (not an include)
         */
        boolean isSourceFile();

        /**
         * @return The stage of the shader being processed
         */
        ShaderStage type();

        GLCapabilities glCapabilities();

        default boolean isVertex() {
            return this.type() == ShaderStage.VERTEX;
        }

        default boolean isFragment() {
            return this.type() == ShaderStage.FRAGMENT;
        }

        default boolean isGeometry() {
            return this.type() == ShaderStage.GEOMETRY;
        }

        default boolean isTessellationControl() {
            return this.type() == ShaderStage.TESS_CONTROL;
        }

        default boolean isTessellationEvaluation() {
            return this.type() == ShaderStage.TESS_EVALUATION;
        }

        /**
         * Includes the specified include file into the tree.
         */
        default void include(GlslTree tree, Identifier name, IncludeOverloadStrategy strategy) throws IOException, GlslSyntaxException, LexerException {
            this.include(tree, name.toString(), this.shaderImporter().loadImport(this, name, false), strategy);
        }

        default void include(GlslTree tree, String name, GlslTree loadedImport, IncludeOverloadStrategy strategy) throws IOException, GlslSyntaxException, LexerException {
            tree.getDirectives().addAll(loadedImport.getDirectives());

            Set<String> fieldNames = loadedImport.fields()
                    .map(GlslNewFieldNode::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
            Set<String> functionNames = loadedImport.functions()
                    .filter(function -> function.getBody() != null)
                    .map(GlslFunctionNode::getName)
                    .collect(Collectors.toUnmodifiableSet());

            switch (strategy) {
                case FAIL -> {
                    for (GlslNode sourceNode : tree.getBody()) {
                        if (!(sourceNode instanceof GlslRootNode rootNode)) {
                            continue;
                        }
                        if (rootNode.isField()) {
                            String fieldName = rootNode.asField().getName();
                            if (fieldName != null && fieldNames.contains(fieldName)) {
                                throw new IOException("Field is part of include '" + name + "': " + fieldName);
                            }
                        } else if (rootNode.isFunction()) {
                            String functionName = rootNode.asFunction().getName();
                            if (functionNames.contains(functionName)) {
                                throw new IOException("Function is part of include '" + name + "': " + functionName);
                            }
                        }
                    }
                }
                case SOURCE -> {
                    for (GlslNode sourceNode : tree.getBody()) {
                        if (!(sourceNode instanceof GlslRootNode rootNode)) {
                            continue;
                        }
                        if (rootNode.isField()) {
                            String fieldName = rootNode.asField().getName();
                            if (fieldName != null && fieldNames.contains(fieldName)) {
                                Set<GlslNewFieldNode> remove = loadedImport.fields()
                                        .filter(node -> fieldName.equals(node.getName()))
                                        .collect(Collectors.toUnmodifiableSet());
                                loadedImport.getBody().removeAll(remove);
                            }
                        } else if (rootNode.isFunction()) {
                            GlslFunctionNode function = rootNode.asFunction();
                            String functionName = function.getName();
                            if (functionNames.contains(functionName)) {
                                Set<GlslFunctionNode> remove = loadedImport.functions()
                                        .filter(node -> node.getName().equals(functionName) && node.getBody() != null && node.getHeader().equals(function.getHeader()))
                                        .collect(Collectors.toUnmodifiableSet());
                                loadedImport.getBody().removeAll(remove);
                            }
                        }
                    }
                }
                case INCLUDE -> {
                    Iterator<GlslNode> iterator = tree.getBody().iterator();
                    while (iterator.hasNext()) {
                        GlslNode sourceNode = iterator.next();
                        if (!(sourceNode instanceof GlslRootNode rootNode)) {
                            continue;
                        }
                        if (rootNode.isField()) {
                            if (fieldNames.contains(rootNode.asField().getName())) {
                                iterator.remove();
                            }
                        } else if (rootNode.isFunction()) {
                            GlslFunctionNode function = rootNode.asFunction();
                            String functionName = function.getName();
                            if (functionNames.contains(functionName) && loadedImport.functions().anyMatch(node -> node.getName().equals(functionName) &&
                                    node.getBody() != null &&
                                    node.getHeader().equals(function.getHeader()))) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }

            tree.getBody().addAll(0, loadedImport.getBody());
        }

        ShaderImporter shaderImporter();

        default ShaderPreDefinitions preDefinitions() {
            return VeilRenderSystem.renderer().getShaderDefinitions();
        }

        /**
         * @return Macros defined for this shader
         */
        Map<String, String> macros();
    }

    /**
     * Context for vanilla core shaders compiled by the 1.21.11 GL backend.
     */
    non-sealed interface MinecraftContext extends Context {

        /**
         * @return The path of the vanilla shader, for example <code>core/terrain</code>
         */
        String shaderInstance();

        /**
         * @return The full id of the vanilla shader, for example <code>minecraft:core/terrain</code>
         */
        Identifier shaderId();

        /**
         * @return The defines the vanilla pipeline compiles this shader with
         */
        ShaderDefines defines();

        /**
         * Checks whether the pipeline compiling this shader declared the specified flag define.
         */
        default boolean hasDefine(String flag) {
            return this.defines().flags().contains(flag) || this.defines().values().containsKey(flag);
        }
    }

    /**
     * Context for Veil shader programs.
     */
    non-sealed interface VeilContext extends Context {

        boolean isDynamic();

        @Nullable
        ProgramDefinition definition();
    }

    enum IncludeOverloadStrategy {
        FAIL,
        SOURCE,
        INCLUDE
    }
}
