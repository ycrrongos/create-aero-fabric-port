package foundry.veil.api.client.render.shader.texture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

/**
 * A texture a Veil program samples from, declared in the program definition <code>textures</code> block.
 */
public sealed interface ShaderTextureSource permits ShaderTextureSource.Location, ShaderTextureSource.Framebuffer {

    /**
     * @return The OpenGL texture id to bind or <code>0</code> if the texture is currently unavailable
     */
    int getId();

    static ShaderTextureSource parse(JsonElement json) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return new Location(Identifier.parse(json.getAsString()));
        }
        JsonObject object = GsonHelper.convertToJsonObject(json, "texture");
        String type = GsonHelper.getAsString(object, "type", "location");
        return switch (type) {
            case "location" -> new Location(Identifier.parse(GsonHelper.getAsString(object, "location")));
            case "framebuffer" -> {
                String name = GsonHelper.getAsString(object, "name");
                boolean depth = name.endsWith(":depth");
                String path = depth ? name.substring(0, name.length() - ":depth".length()) : name;
                Identifier location = path.contains(":") ? Identifier.parse(path) : Identifier.fromNamespaceAndPath("temp", path);
                yield new Framebuffer(location, depth ? 0 : GsonHelper.getAsInt(object, "sampler", 0), depth);
            }
            default -> throw new JsonParseException("Unknown texture source type: " + type);
        };
    }

    record Location(Identifier location) implements ShaderTextureSource {

        @Override
        public int getId() {
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(this.location);
            return VeilRenderSystem.getTextureId(texture.getTextureView());
        }
    }

    record Framebuffer(Identifier name, int sampler, boolean depth) implements ShaderTextureSource {

        @Override
        public int getId() {
            @Nullable AdvancedFbo framebuffer = VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(this.name);
            if (framebuffer == null) {
                return 0;
            }
            if (this.depth) {
                return framebuffer.isDepthTextureAttachment() ? framebuffer.getDepthTextureAttachment().getId() : 0;
            }
            return framebuffer.isColorTextureAttachment(this.sampler) ? framebuffer.getColorTextureAttachment(this.sampler).getId() : 0;
        }
    }
}
