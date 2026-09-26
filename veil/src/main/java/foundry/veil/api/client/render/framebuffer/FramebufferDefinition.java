package foundry.veil.api.client.render.framebuffer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.textures.TextureFormat;
import foundry.veil.impl.client.render.pipeline.VeilSizeExpression;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The definition of a framebuffer loaded from <code>pinwheel/framebuffers</code> or declared inline in a post pipeline.
 *
 * @param width        The width expression
 * @param height       The height expression
 * @param colorFormats The format of every color attachment
 * @param depth        Whether a depth texture is attached
 * @param autoClear    Whether the framebuffer is cleared at the start of every frame
 */
public record FramebufferDefinition(VeilSizeExpression width, VeilSizeExpression height, List<TextureFormat> colorFormats, boolean depth, boolean autoClear) {

    private static final VeilSizeExpression SCREEN_WIDTH = new VeilSizeExpression("q.screen_width");
    private static final VeilSizeExpression SCREEN_HEIGHT = new VeilSizeExpression("q.screen_height");

    public AdvancedFbo create(String name, int screenWidth, int screenHeight) {
        AdvancedFbo.Builder builder = AdvancedFbo.withSize(this.width.evaluate(screenWidth, screenHeight), this.height.evaluate(screenWidth, screenHeight)).setName(name);
        for (TextureFormat format : this.colorFormats) {
            builder.addColorTextureBuffer(format);
        }
        if (this.depth) {
            builder.setDepthTextureBuffer();
        }
        return builder.build(true);
    }

    public static FramebufferDefinition parse(JsonObject json) throws JsonParseException {
        VeilSizeExpression width = parseSize(json.get("width"), SCREEN_WIDTH);
        VeilSizeExpression height = parseSize(json.get("height"), SCREEN_HEIGHT);
        List<TextureFormat> formats = new ArrayList<>();
        if (json.has("color_buffers")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "color_buffers")) {
                JsonObject buffer = GsonHelper.convertToJsonObject(element, "color_buffers");
                formats.add(parseFormat(GsonHelper.getAsString(buffer, "format", "RGBA8")));
            }
        } else {
            formats.add(parseFormat(GsonHelper.getAsString(json, "format", "RGBA8")));
        }
        boolean depth;
        JsonElement depthElement = json.get("depth");
        if (depthElement == null) {
            depth = false;
        } else if (depthElement.isJsonPrimitive()) {
            depth = depthElement.getAsBoolean();
        } else {
            depth = true;
        }
        boolean autoClear = GsonHelper.getAsBoolean(json, "autoClear", true);
        return new FramebufferDefinition(width, height, formats, depth, autoClear);
    }

    private static VeilSizeExpression parseSize(JsonElement element, VeilSizeExpression fallback) {
        if (element == null) {
            return fallback;
        }
        return new VeilSizeExpression(element.getAsString());
    }

    /**
     * Maps the Veil attachment formats to the formats supported by the 1.21.11 texture API. Floating point formats are
     * stored as 8-bit normalized color, which is what the main target they are resolved into uses.
     */
    private static TextureFormat parseFormat(String name) {
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "R8", "RED", "RED8" -> TextureFormat.RED8;
            default -> TextureFormat.RGBA8;
        };
    }
}
