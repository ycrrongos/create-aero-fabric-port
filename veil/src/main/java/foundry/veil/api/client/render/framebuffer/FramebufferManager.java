package foundry.veil.api.client.render.framebuffer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.Veil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;

/**
 * Manages every framebuffer defined in <code>pinwheel/framebuffers</code>.
 * <p>
 * <code>minecraft:main</code> always refers to the vanilla main target.
 */
public class FramebufferManager implements PreparableReloadListener, NativeResource {

    public static final String FOLDER = "pinwheel/framebuffers";
    public static final Identifier MAIN = Identifier.withDefaultNamespace("main");

    private final Map<Identifier, FramebufferDefinition> definitions = new HashMap<>();
    private final Map<Identifier, AdvancedFbo> framebuffers = new HashMap<>();
    private final Map<Identifier, AdvancedFbo> manualFramebuffers = new HashMap<>();
    private int screenWidth = -1;
    private int screenHeight = -1;

    /**
     * @return The framebuffer with the specified name or <code>null</code> if it isn't defined
     */
    public @Nullable AdvancedFbo getFramebuffer(Identifier name) {
        if (MAIN.equals(name)) {
            return AdvancedFbo.getMainFramebuffer();
        }
        AdvancedFbo manual = this.manualFramebuffers.get(name);
        if (manual != null) {
            return manual;
        }
        this.updateScreenSize();
        AdvancedFbo framebuffer = this.framebuffers.get(name);
        if (framebuffer == null) {
            FramebufferDefinition definition = this.definitions.get(name);
            if (definition == null) {
                return null;
            }
            framebuffer = definition.create(name.toString(), this.screenWidth, this.screenHeight);
            this.framebuffers.put(name, framebuffer);
        }
        return framebuffer;
    }

    public @Nullable FramebufferDefinition getFramebufferDefinition(Identifier name) {
        return this.definitions.get(name);
    }

    /**
     * Registers a framebuffer created by code under the specified name.
     */
    public void setFramebuffer(Identifier name, AdvancedFbo framebuffer) {
        this.manualFramebuffers.put(name, framebuffer);
    }

    public void removeFramebuffer(Identifier name) {
        this.manualFramebuffers.remove(name);
    }

    /**
     * @return Every currently created framebuffer, including <code>minecraft:main</code>
     */
    public Map<Identifier, AdvancedFbo> getFramebuffers() {
        Map<Identifier, AdvancedFbo> all = new HashMap<>(this.framebuffers);
        all.putAll(this.manualFramebuffers);
        all.put(MAIN, AdvancedFbo.getMainFramebuffer());
        return Collections.unmodifiableMap(all);
    }

    private void updateScreenSize() {
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (main.width != this.screenWidth || main.height != this.screenHeight) {
            this.screenWidth = main.width;
            this.screenHeight = main.height;
            this.framebuffers.values().forEach(AdvancedFbo::free);
            this.framebuffers.clear();
        }
    }

    /**
     * Clears every framebuffer that requests to be cleared automatically. Called at the start of every level render.
     */
    public void clear() {
        this.updateScreenSize();
        for (Map.Entry<Identifier, AdvancedFbo> entry : this.framebuffers.entrySet()) {
            FramebufferDefinition definition = this.definitions.get(entry.getKey());
            if (definition != null && definition.autoClear()) {
                entry.getValue().clear(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            }
        }
    }

    @Override
    public CompletableFuture<Void> reload(SharedState state, Executor backgroundExecutor, PreparationBarrier barrier, Executor gameExecutor) {
        ResourceManager resourceManager = state.resourceManager();
        return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, FramebufferDefinition> definitions = new HashMap<>();
                    for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(FOLDER, id -> id.getPath().endsWith(".json")).entrySet()) {
                        Identifier file = entry.getKey();
                        String path = file.getPath();
                        Identifier id = file.withPath(path.substring(FOLDER.length() + 1, path.length() - ".json".length()));
                        try (Reader reader = entry.getValue().openAsReader()) {
                            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                            definitions.put(id, FramebufferDefinition.parse(json));
                        } catch (Exception e) {
                            Veil.LOGGER.error("Failed to load framebuffer definition {}", file, e);
                        }
                    }
                    return definitions;
                }, backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(definitions -> {
                    this.framebuffers.values().forEach(AdvancedFbo::free);
                    this.framebuffers.clear();
                    this.definitions.clear();
                    this.definitions.putAll(definitions);
                    Veil.LOGGER.info("Loaded {} framebuffer definitions", definitions.size());
                }, gameExecutor);
    }

    @Override
    public void free() {
        this.framebuffers.values().forEach(AdvancedFbo::free);
        this.framebuffers.clear();
    }
}
