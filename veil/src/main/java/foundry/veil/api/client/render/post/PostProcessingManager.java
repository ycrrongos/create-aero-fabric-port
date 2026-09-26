package foundry.veil.api.client.render.post;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilDrawState;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.platform.VeilClientPlatform;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;

/**
 * Loads post-processing pipelines from <code>pinwheel/post</code> and runs them.
 */
public class PostProcessingManager implements PreparableReloadListener, NativeResource {

    public static final String FOLDER = "pinwheel/post";

    private final Map<Identifier, CompositePostPipeline> pipelines = new HashMap<>();
    private final List<Identifier> activePipelines = new ArrayList<>();
    private final ContextImpl context = new ContextImpl();

    public @Nullable PostPipeline getPipeline(Identifier name) {
        return this.pipelines.get(name);
    }

    public Set<Identifier> getPipelines() {
        return Collections.unmodifiableSet(this.pipelines.keySet());
    }

    /**
     * @return The context used when running pipelines. Framebuffers set on it persist between runs
     */
    public PostPipeline.Context getPostPipelineContext() {
        return this.context;
    }

    public boolean isActive(Identifier pipeline) {
        return this.activePipelines.contains(pipeline);
    }

    /**
     * Adds a pipeline that is run automatically after the level is rendered.
     */
    public boolean add(Identifier pipeline) {
        if (this.activePipelines.contains(pipeline)) {
            return false;
        }
        this.activePipelines.add(pipeline);
        return true;
    }

    public boolean remove(Identifier pipeline) {
        return this.activePipelines.remove(pipeline);
    }

    /**
     * Runs every active pipeline. Called after the level has been rendered.
     */
    public void runDefaultPipeline() {
        for (Identifier id : List.copyOf(this.activePipelines)) {
            CompositePostPipeline pipeline = this.pipelines.get(id);
            if (pipeline != null) {
                VeilClientPlatform.INSTANCE.preVeilPostProcessing(id, pipeline, this.context);
                this.runPipeline(pipeline, true);
                VeilClientPlatform.INSTANCE.postVeilPostProcessing(id, pipeline, this.context);
            }
        }
    }

    /**
     * Runs the pipeline and copies the post framebuffer into the main framebuffer.
     */
    public void runPipeline(@Nullable PostPipeline pipeline) {
        this.runPipeline(pipeline, true);
    }

    /**
     * Runs the pipeline.
     *
     * @param resolvePost Whether to copy the post framebuffer into the main framebuffer afterward
     */
    public void runPipeline(@Nullable PostPipeline pipeline, boolean resolvePost) {
        if (pipeline == null) {
            return;
        }
        AdvancedFbo previous = AdvancedFbo.getBound();
        this.context.begin();
        try {
            pipeline.apply(this.context);
        } catch (Exception e) {
            Veil.LOGGER.error("Error running post pipeline {}", pipeline, e);
        } finally {
            this.context.end();
        }

        if (resolvePost) {
            AdvancedFbo post = VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(PostStages.POST);
            if (post != null) {
                post.resolveToAdvancedFbo(AdvancedFbo.getMainFramebuffer(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
            }
        }

        if (previous != null) {
            previous.bind(true);
        } else {
            AdvancedFbo.unbind();
        }
    }

    @Override
    public CompletableFuture<Void> reload(SharedState state, Executor backgroundExecutor, PreparationBarrier barrier, Executor gameExecutor) {
        ResourceManager resourceManager = state.resourceManager();
        return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, CompositePostPipeline> pipelines = new HashMap<>();
                    for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(FOLDER, id -> id.getPath().endsWith(".json")).entrySet()) {
                        Identifier file = entry.getKey();
                        String path = file.getPath();
                        Identifier id = file.withPath(path.substring(FOLDER.length() + 1, path.length() - ".json".length()));
                        try (Reader reader = entry.getValue().openAsReader()) {
                            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                            pipelines.put(id, CompositePostPipeline.parse(json));
                        } catch (Exception e) {
                            Veil.LOGGER.error("Failed to load post pipeline {}", file, e);
                        }
                    }
                    return pipelines;
                }, backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(pipelines -> {
                    this.pipelines.values().forEach(CompositePostPipeline::free);
                    this.pipelines.clear();
                    this.pipelines.putAll(pipelines);
                    Veil.LOGGER.info("Loaded {} post pipelines", pipelines.size());
                }, gameExecutor);
    }

    @Override
    public void free() {
        this.pipelines.values().forEach(CompositePostPipeline::free);
        this.pipelines.clear();
    }

    private static final class ContextImpl implements PostPipeline.Context {

        private final Map<Identifier, AdvancedFbo> persistentFramebuffers = new HashMap<>();
        private final Map<Identifier, AdvancedFbo> framebuffers = new HashMap<>();
        private final Map<String, Integer> textures = new HashMap<>();
        private final VeilDrawState drawState = new VeilDrawState();

        private void begin() {
            this.framebuffers.clear();
            this.framebuffers.putAll(VeilRenderSystem.renderer().getFramebufferManager().getFramebuffers());
            this.framebuffers.putAll(this.persistentFramebuffers);
            this.textures.clear();
            this.drawState.reset().disableDepthTest().disableCull().disableBlend().depthMask(false);
        }

        private void end() {
            this.textures.clear();
        }

        @Override
        public @Nullable AdvancedFbo getFramebuffer(Identifier name) {
            AdvancedFbo framebuffer = this.framebuffers.get(name);
            if (framebuffer == null) {
                framebuffer = VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(name);
            }
            return framebuffer;
        }

        @Override
        public AdvancedFbo getFramebufferOrDraw(Identifier name) {
            AdvancedFbo framebuffer = this.getFramebuffer(name);
            if (framebuffer != null) {
                return framebuffer;
            }
            AdvancedFbo post = VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(PostStages.POST);
            return post != null ? post : AdvancedFbo.getMainFramebuffer();
        }

        @Override
        public void setFramebuffer(Identifier name, AdvancedFbo framebuffer) {
            this.framebuffers.put(name, framebuffer);
            if (!"temp".equals(name.getNamespace())) {
                this.persistentFramebuffers.put(name, framebuffer);
            }
        }

        @Override
        public void setTexture(CharSequence name, int textureId) {
            this.textures.put(name.toString(), textureId);
        }

        @Override
        public void applySamplers(ShaderProgram program) {
            for (Map.Entry<String, Integer> entry : this.textures.entrySet()) {
                program.setSampler(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public VeilDrawState drawState() {
            return this.drawState;
        }
    }
}
