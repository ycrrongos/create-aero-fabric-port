package foundry.veil.api.client.render.post;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferDefinition;
import foundry.veil.api.client.render.shader.texture.ShaderTextureSource;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.lwjgl.system.NativeResource;

import java.util.*;

/**
 * A pipeline made of several stages, with its own framebuffers and textures.
 */
public final class CompositePostPipeline implements PostPipeline, NativeResource {

    private final List<PostPipeline> stages;
    private final Map<Identifier, FramebufferDefinition> framebufferDefinitions;
    private final Map<String, ShaderTextureSource> textures;
    private final Map<Identifier, AdvancedFbo> framebuffers = new HashMap<>();
    private int screenWidth = -1;
    private int screenHeight = -1;

    public CompositePostPipeline(List<PostPipeline> stages, Map<Identifier, FramebufferDefinition> framebufferDefinitions, Map<String, ShaderTextureSource> textures) {
        this.stages = stages;
        this.framebufferDefinitions = framebufferDefinitions;
        this.textures = textures;
    }

    public static CompositePostPipeline parse(JsonObject json) throws JsonParseException {
        List<PostPipeline> stages = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "stages")) {
            stages.add(PostStages.parse(GsonHelper.convertToJsonObject(element, "stage")));
        }
        Map<Identifier, FramebufferDefinition> framebuffers = new LinkedHashMap<>();
        if (json.has("framebuffers")) {
            for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "framebuffers").entrySet()) {
                framebuffers.put(PostStages.parseFramebuffer(entry.getKey()), FramebufferDefinition.parse(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey())));
            }
        }
        Map<String, ShaderTextureSource> textures = new LinkedHashMap<>();
        if (json.has("textures")) {
            for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "textures").entrySet()) {
                textures.put(entry.getKey(), ShaderTextureSource.parse(entry.getValue()));
            }
        }
        return new CompositePostPipeline(List.copyOf(stages), Collections.unmodifiableMap(framebuffers), Collections.unmodifiableMap(textures));
    }

    private void updateFramebuffers() {
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (main.width == this.screenWidth && main.height == this.screenHeight && this.framebuffers.size() == this.framebufferDefinitions.size()) {
            return;
        }
        this.free();
        this.screenWidth = main.width;
        this.screenHeight = main.height;
        for (Map.Entry<Identifier, FramebufferDefinition> entry : this.framebufferDefinitions.entrySet()) {
            this.framebuffers.put(entry.getKey(), entry.getValue().create(entry.getKey().toString(), this.screenWidth, this.screenHeight));
        }
    }

    @Override
    public void apply(Context context) {
        this.updateFramebuffers();
        this.framebuffers.forEach(context::setFramebuffer);
        for (Map.Entry<String, ShaderTextureSource> entry : this.textures.entrySet()) {
            context.setTexture(entry.getKey(), entry.getValue().getId());
        }
        for (PostPipeline stage : this.stages) {
            stage.apply(context);
        }
    }

    public List<PostPipeline> getStages() {
        return this.stages;
    }

    @Override
    public void free() {
        this.framebuffers.values().forEach(AdvancedFbo::free);
        this.framebuffers.clear();
        this.screenWidth = -1;
        this.screenHeight = -1;
    }
}
