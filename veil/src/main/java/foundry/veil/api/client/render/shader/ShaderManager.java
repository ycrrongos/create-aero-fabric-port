package foundry.veil.api.client.render.shader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.processor.ShaderImporter;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.render.shader.VeilShaderPreProcessorList;
import foundry.veil.impl.client.render.shader.VeilShaderSources;
import foundry.veil.platform.VeilClientPlatform;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.NativeResource;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL20C.*;

/**
 * Loads, pre-processes and links every Veil program found in <code>pinwheel/shaders/program</code>.
 */
public class ShaderManager implements PreparableReloadListener, NativeResource {

    private final ShaderPreDefinitions definitions;
    private final VeilShaderPreProcessorList processors;
    private final Map<Identifier, ShaderProgram> programs = new HashMap<>();
    private final Map<Identifier, ProgramDefinition> programDefinitions = new HashMap<>();
    private final Map<Identifier, Set<String>> programDependencies = new HashMap<>();
    private final Set<Identifier> dirtyPrograms = new HashSet<>();
    private VeilShaderSources sources = new VeilShaderSources(Map.of(), Map.of());

    public ShaderManager(ShaderPreDefinitions definitions, VeilShaderPreProcessorList processors) {
        this.definitions = definitions;
        this.processors = processors;
        this.definitions.addListener(name -> {
            for (Map.Entry<Identifier, Set<String>> entry : this.programDependencies.entrySet()) {
                if (entry.getValue().contains(name)) {
                    this.dirtyPrograms.add(entry.getKey());
                }
            }
        });
    }

    /**
     * @return The program with the specified name or <code>null</code> if it failed to compile or does not exist
     */
    public @Nullable ShaderProgram getShader(Identifier name) {
        this.recompileDirty();
        return this.programs.get(name);
    }

    public Map<Identifier, ShaderProgram> getShaders() {
        return Collections.unmodifiableMap(this.programs);
    }

    public ShaderPreDefinitions getDefinitions() {
        return this.definitions;
    }

    public VeilShaderSources getSources() {
        return this.sources;
    }

    /**
     * Recompiles every program whose definitions changed since the last compile.
     */
    public void recompileDirty() {
        if (this.dirtyPrograms.isEmpty()) {
            return;
        }
        Set<Identifier> dirty = new HashSet<>(this.dirtyPrograms);
        this.dirtyPrograms.clear();
        Map<Identifier, ShaderProgram> updated = new HashMap<>();
        for (Identifier id : dirty) {
            ProgramDefinition definition = this.programDefinitions.get(id);
            if (definition == null) {
                continue;
            }
            ShaderProgram program = this.compile(id, definition);
            ShaderProgram old = program != null ? this.programs.put(id, program) : this.programs.remove(id);
            if (old != null) {
                old.free();
            }
            if (program != null) {
                updated.put(id, program);
            }
        }
        VeilClientPlatform.INSTANCE.onVeilCompileShaders(this, updated);
    }

    private @Nullable ShaderProgram compile(Identifier id, ProgramDefinition definition) {
        Set<String> dependencies = new HashSet<>();
        Map<String, String> macros = definition.getMacros(dependencies, this.definitions);
        this.programDependencies.put(id, dependencies);

        boolean tessellation = definition.hasTessellation();
        if (tessellation && !VeilRenderSystem.tessellationSupported()) {
            Veil.LOGGER.warn("Skipping program {}: tessellation shaders are not supported by this GPU", id);
            return null;
        }

        List<Integer> shaders = new ArrayList<>();
        int program = glCreateProgram();
        try {
            for (Map.Entry<ShaderStage, Identifier> stageEntry : definition.stages().entrySet()) {
                ShaderStage stage = stageEntry.getKey();
                Identifier sourceId = stageEntry.getValue();
                String raw = this.sources.getSource(sourceId, stage.getExtension());
                if (raw == null) {
                    throw new IOException("Missing " + stage.getJsonKey() + " shader source: " + sourceId);
                }
                String source = this.processSource(id, definition, stage, sourceId, raw, macros, tessellation);
                int shader = glCreateShader(stage.getGlType());
                glShaderSource(shader, source);
                glCompileShader(shader);
                if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
                    String log = glGetShaderInfoLog(shader);
                    glDeleteShader(shader);
                    if (Veil.VERBOSE_SHADER_ERRORS) {
                        Veil.LOGGER.error("Source of failed {} shader {}:\n{}", stage.getJsonKey(), sourceId, source);
                    }
                    throw new IOException("Failed to compile " + stage.getJsonKey() + " shader " + sourceId + ": " + log);
                }
                glAttachShader(program, shader);
                shaders.add(shader);
            }

            glLinkProgram(program);
            if (glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE) {
                throw new IOException("Failed to link program " + id + ": " + glGetProgramInfoLog(program));
            }
            for (int shader : shaders) {
                glDetachShader(program, shader);
                glDeleteShader(shader);
            }
            return ShaderProgram.create(id, definition, program, tessellation);
        } catch (Exception e) {
            for (int shader : shaders) {
                glDeleteShader(shader);
            }
            glDeleteProgram(program);
            Veil.LOGGER.error("Failed to compile Veil program {}", id, e);
            return null;
        }
    }

    private String processSource(Identifier programId, ProgramDefinition definition, ShaderStage stage, Identifier sourceId, String raw, Map<String, String> macros, boolean tessellation) throws IOException {
        Set<Identifier> included = new HashSet<>();
        String expanded = this.sources.expandIncludes(raw, included);
        String assembled = VeilShaderSources.assemble(expanded, macros, tessellation);
        if (this.processors.isEmpty()) {
            return assembled;
        }

        try {
            GlslTree tree = GlslParser.preprocessParse(assembled, new HashMap<>());
            ShaderImporter importer = this.createImporter();
            ShaderPreProcessor.VeilContext context = new ShaderPreProcessor.VeilContext() {
                @Override
                public boolean isDynamic() {
                    return false;
                }

                @Override
                public ProgramDefinition definition() {
                    return definition;
                }

                @Override
                public Identifier name() {
                    return sourceId;
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
                    return macros;
                }
            };
            this.processors.processor().modify(context, tree);
            return tree.toSourceString();
        } catch (Exception e) {
            Veil.LOGGER.error("Failed to apply shader pre-processors to {} ({}), using unprocessed source", programId, stage.getJsonKey(), e);
            return assembled;
        }
    }

    /**
     * Creates an importer that loads Veil include files as parsed trees.
     */
    public ShaderImporter createImporter() {
        Set<Identifier> added = new LinkedHashSet<>();
        return new ShaderImporter() {
            @Override
            public GlslTree loadImport(ShaderPreProcessor.Context context, Identifier name, boolean force) throws IOException {
                String include = ShaderManager.this.sources.getInclude(name);
                if (include == null) {
                    throw new IOException("Unknown shader include: " + name);
                }
                added.add(name);
                try {
                    String expanded = ShaderManager.this.sources.expandIncludes(include, new HashSet<>(Set.of(name)));
                    return GlslParser.preprocessParse(VeilShaderSources.stripVersion(expanded), new HashMap<>());
                } catch (Exception e) {
                    throw new IOException("Failed to parse include " + name, e);
                }
            }

            @Override
            public Collection<Identifier> addedImports() {
                return Collections.unmodifiableSet(added);
            }
        };
    }

    @Override
    public CompletableFuture<Void> reload(SharedState state, Executor backgroundExecutor, PreparationBarrier barrier, Executor gameExecutor) {
        ResourceManager resourceManager = state.resourceManager();
        return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, ProgramDefinition> definitions = new HashMap<>();
                    for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(VeilShaderSources.PROGRAM_FOLDER, id -> id.getPath().endsWith(".json")).entrySet()) {
                        Identifier file = entry.getKey();
                        String path = file.getPath();
                        Identifier id = file.withPath(path.substring(VeilShaderSources.PROGRAM_FOLDER.length() + 1, path.length() - ".json".length()));
                        try (Reader reader = entry.getValue().openAsReader()) {
                            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                            definitions.put(id, ProgramDefinition.parse(json));
                        } catch (Exception e) {
                            Veil.LOGGER.error("Failed to load Veil program definition {}", file, e);
                        }
                    }
                    return new Prepared(definitions, VeilShaderSources.load(resourceManager));
                }, backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(prepared -> this.apply(prepared, resourceManager), gameExecutor);
    }

    private void apply(Prepared prepared, ResourceManager resourceManager) {
        RenderSystem.assertOnRenderThread();
        this.processors.reload(resourceManager);
        this.sources = prepared.sources();
        this.programDefinitions.clear();
        this.programDefinitions.putAll(prepared.definitions());
        this.dirtyPrograms.clear();

        Map<Identifier, ShaderProgram> old = new HashMap<>(this.programs);
        this.programs.clear();
        for (Map.Entry<Identifier, ProgramDefinition> entry : this.programDefinitions.entrySet()) {
            ShaderProgram program = this.compile(entry.getKey(), entry.getValue());
            if (program != null) {
                this.programs.put(entry.getKey(), program);
            }
        }
        old.values().forEach(ShaderProgram::free);
        Veil.LOGGER.info("Compiled {}/{} Veil shader programs", this.programs.size(), this.programDefinitions.size());
        VeilClientPlatform.INSTANCE.onVeilCompileShaders(this, Collections.unmodifiableMap(this.programs));
    }

    @Override
    public void free() {
        this.programs.values().forEach(ShaderProgram::free);
        this.programs.clear();
    }

    private record Prepared(Map<Identifier, ProgramDefinition> definitions, VeilShaderSources sources) {
    }
}
