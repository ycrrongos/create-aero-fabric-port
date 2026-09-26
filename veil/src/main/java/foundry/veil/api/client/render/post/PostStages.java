package foundry.veil.api.client.render.post;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.opengl.GL11C.*;

/**
 * The post-processing stage types supported by pipeline JSON files.
 */
public final class PostStages {

    public static final Identifier POST = Veil.veilPath("post");

    private PostStages() {
    }

    /**
     * Parses a framebuffer reference. Names without a namespace refer to pipeline-local framebuffers.
     */
    public static Identifier parseFramebuffer(String name) {
        return name.contains(":") ? Identifier.parse(name) : Identifier.fromNamespaceAndPath("temp", name);
    }

    public static PostPipeline parse(JsonObject json) throws JsonParseException {
        String type = GsonHelper.getAsString(json, "type");
        return switch (type) {
            case "veil:blit", "blit" -> new Blit(Identifier.parse(GsonHelper.getAsString(json, "shader")),
                    json.has("in") ? parseFramebuffer(GsonHelper.getAsString(json, "in")) : null,
                    json.has("out") ? parseFramebuffer(GsonHelper.getAsString(json, "out")) : POST,
                    GsonHelper.getAsBoolean(json, "clear", true));
            case "veil:mask", "mask" -> new Mask(GsonHelper.getAsBoolean(json, "red", true),
                    GsonHelper.getAsBoolean(json, "green", true),
                    GsonHelper.getAsBoolean(json, "blue", true),
                    GsonHelper.getAsBoolean(json, "alpha", true),
                    GsonHelper.getAsBoolean(json, "depth", false));
            case "veil:copy", "copy" -> new Copy(parseFramebuffer(GsonHelper.getAsString(json, "in")),
                    parseFramebuffer(GsonHelper.getAsString(json, "out")),
                    GsonHelper.getAsBoolean(json, "color", true),
                    GsonHelper.getAsBoolean(json, "depth", false),
                    GsonHelper.getAsBoolean(json, "linear", false));
            default -> throw new JsonParseException("Unknown post stage type: " + type);
        };
    }

    /**
     * Draws a full screen quad with a shader, sampling from an input framebuffer.
     */
    public record Blit(Identifier shader, @Nullable Identifier in, Identifier out, boolean clear) implements PostPipeline {

        @Override
        public void apply(Context context) {
            ShaderProgram program = VeilRenderSystem.renderer().getShaderManager().getShader(this.shader);
            if (program == null) {
                Veil.LOGGER.warn("Failed to find post shader: {}", this.shader);
                return;
            }

            AdvancedFbo in = this.in != null ? context.getFramebuffer(this.in) : null;
            AdvancedFbo out = context.getFramebufferOrDraw(this.out);

            context.applySamplers(program);
            if (in != null) {
                for (int i = 0; i < in.getColorAttachments(); i++) {
                    int id = in.getColorTextureAttachment(i).getId();
                    program.setSampler("DiffuseSampler" + i, id);
                    if (i == 0) {
                        program.setSampler("DiffuseSampler", id);
                    }
                }
                if (in.isDepthTextureAttachment()) {
                    program.setSampler("DiffuseDepthSampler", in.getDepthTextureAttachment().getId());
                }
            }

            out.bind(true);
            if (this.clear) {
                out.clear();
            }

            program.setDefaultUniforms(new org.joml.Matrix4f(), VeilRenderSystem.getProjectionMatrix());
            ShaderUniform inSize = program.getUniform("InSize");
            if (inSize != null) {
                if (in != null) {
                    inSize.setVector(in.getWidth(), in.getHeight());
                } else {
                    inSize.setVector(1.0F, 1.0F);
                }
            }
            ShaderUniform outSize = program.getUniform("OutSize");
            if (outSize != null) {
                outSize.setVector(out.getWidth(), out.getHeight());
            }

            VeilRenderSystem.drawScreenQuad(program, context.drawState());
            program.clearSamplers();
        }
    }

    /**
     * Changes the write mask of the following stages.
     */
    public record Mask(boolean red, boolean green, boolean blue, boolean alpha, boolean depth) implements PostPipeline {

        @Override
        public void apply(Context context) {
            context.drawState().colorMask(this.red, this.green, this.blue, this.alpha).depthMask(this.depth);
        }
    }

    /**
     * Copies one framebuffer into another.
     */
    public record Copy(Identifier in, Identifier out, boolean color, boolean depth, boolean linear) implements PostPipeline {

        @Override
        public void apply(Context context) {
            AdvancedFbo in = context.getFramebuffer(this.in);
            AdvancedFbo out = context.getFramebuffer(this.out);
            if (in == null || out == null) {
                return;
            }
            int mask = (this.color ? GL_COLOR_BUFFER_BIT : 0) | (this.depth ? GL_DEPTH_BUFFER_BIT : 0);
            in.resolveToAdvancedFbo(out, mask, this.linear && !this.depth ? GL_LINEAR : GL_NEAREST);
        }
    }
}
