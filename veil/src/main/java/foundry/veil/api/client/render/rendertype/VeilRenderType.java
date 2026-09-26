package foundry.veil.api.client.render.rendertype;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilDrawState;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.render.VeilImmediateRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;

/**
 * A {@link RenderType} that draws its geometry with a Veil shader program instead of a vanilla pipeline.
 * <p>
 * Geometry can be submitted exactly like for any other render type (through a {@link net.minecraft.client.renderer.MultiBufferSource}
 * or a {@link net.minecraft.client.renderer.SubmitNodeCollector}); when the batch is flushed the program is bound, the
 * configured textures and fixed-function state are applied, and the mesh is drawn.
 */
public class VeilRenderType extends RenderType {

    private final Identifier shader;
    private final Map<String, Supplier<Integer>> textures;
    private final VeilDrawState state;
    private final int patchVertices;
    private final boolean lightmap;
    private final boolean overlay;
    @Nullable
    private final Consumer<ShaderProgram> setup;
    @Nullable
    private final Supplier<Boolean> enabled;

    protected VeilRenderType(String name, RenderSetup setup, Identifier shader, Map<String, Supplier<Integer>> textures,
                             VeilDrawState state, int patchVertices, boolean lightmap, boolean overlay,
                             @Nullable Consumer<ShaderProgram> programSetup, @Nullable Supplier<Boolean> enabled) {
        super(name, setup);
        this.shader = shader;
        this.textures = textures;
        this.state = state;
        this.patchVertices = patchVertices;
        this.lightmap = lightmap;
        this.overlay = overlay;
        this.setup = programSetup;
        this.enabled = enabled;
    }

    public static Builder builder(String name, Identifier shader) {
        return new Builder(name, shader);
    }

    /**
     * @return The name of the specified render type
     */
    public static String getName(RenderType renderType) {
        return renderType.name;
    }

    public Identifier getShaderId() {
        return this.shader;
    }

    public VeilDrawState getState() {
        return this.state;
    }

    public int getPatchVertices() {
        return this.patchVertices;
    }

    public boolean isEnabled() {
        return this.enabled == null || this.enabled.get();
    }

    /**
     * Binds the program of this render type and sets up all of its textures and uniforms.
     *
     * @return The program or <code>null</code> if this render type can't currently be drawn
     */
    public @Nullable ShaderProgram setupProgram() {
        if (!this.isEnabled()) {
            return null;
        }
        ShaderProgram program = VeilRenderSystem.renderer().getShaderManager().getShader(this.shader);
        if (program == null) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        for (Map.Entry<String, Supplier<Integer>> entry : this.textures.entrySet()) {
            program.setSampler(entry.getKey(), entry.getValue().get());
        }
        if (this.overlay) {
            program.setSampler("Sampler1", minecraft.gameRenderer.overlayTexture().getTextureView());
        }
        if (this.lightmap) {
            program.setSampler("Sampler2", minecraft.gameRenderer.lightTexture().getTextureView());
        }
        program.setDefaultUniforms(RenderSystem.getModelViewMatrix(), VeilRenderSystem.getProjectionMatrix());
        if (this.setup != null) {
            this.setup.accept(program);
        }
        return program;
    }

    @Override
    public void draw(MeshData meshData) {
        ShaderProgram program = this.setupProgram();
        if (program == null) {
            meshData.close();
            return;
        }

        GpuTextureView colorOverride = RenderSystem.outputColorTextureOverride;
        AdvancedFbo bound = AdvancedFbo.getBound();
        if (colorOverride != null && bound == null) {
            // Vanilla redirected output to a texture (for example GUI picture-in-picture rendering)
            GpuTextureView depthOverride = RenderSystem.outputDepthTextureOverride;
            int fbo = ((GlTextureView) colorOverride).getFbo(VeilRenderSystem.directStateAccess(), depthOverride != null ? depthOverride.texture() : null);
            VeilImmediateRenderer.drawToFramebuffer(meshData, program, this.state, this.patchVertices, fbo, colorOverride.getWidth(0), colorOverride.getHeight(0));
        } else {
            VeilImmediateRenderer.draw(meshData, program, this.state, this.patchVertices);
        }
        program.clearSamplers();
    }

    public static class Builder {

        private final String name;
        private final Identifier shader;
        private final Map<String, Supplier<Integer>> textures = new LinkedHashMap<>();
        private VertexFormat format;
        private VertexFormat.Mode mode = VertexFormat.Mode.QUADS;
        private VeilDrawState state = new VeilDrawState();
        private int patchVertices;
        private int bufferSize = TRANSIENT_BUFFER_SIZE;
        private boolean sortOnUpload;
        private boolean affectsCrumbling;
        private boolean lightmap;
        private boolean overlay;
        @Nullable
        private Consumer<ShaderProgram> setup;
        @Nullable
        private Supplier<Boolean> enabled;

        private Builder(String name, Identifier shader) {
            this.name = name;
            this.shader = shader;
        }

        public Builder format(VertexFormat format, VertexFormat.Mode mode) {
            this.format = format;
            this.mode = mode;
            return this;
        }

        public Builder texture(String sampler, Identifier location) {
            this.textures.put(sampler, () -> VeilRenderSystem.getTextureId(Minecraft.getInstance().getTextureManager().getTexture(location).getTextureView()));
            return this;
        }

        public Builder texture(String sampler, Supplier<Integer> textureId) {
            this.textures.put(sampler, textureId);
            return this;
        }

        public Builder lightmap() {
            this.lightmap = true;
            return this;
        }

        public Builder overlay() {
            this.overlay = true;
            return this;
        }

        public Builder state(VeilDrawState state) {
            this.state = state.copy();
            return this;
        }

        public Builder state(Consumer<VeilDrawState> modifier) {
            modifier.accept(this.state);
            return this;
        }

        /**
         * Draws the geometry as tessellation patches with the specified number of vertices each.
         */
        public Builder patchVertices(int patchVertices) {
            this.patchVertices = patchVertices;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder sortOnUpload() {
            this.sortOnUpload = true;
            return this;
        }

        public Builder affectsCrumbling() {
            this.affectsCrumbling = true;
            return this;
        }

        /**
         * Adds a callback run every time the program is bound for this render type, to set custom uniforms.
         */
        public Builder setup(Consumer<ShaderProgram> setup) {
            this.setup = setup;
            return this;
        }

        /**
         * Adds a condition that must be true for the geometry to be drawn.
         */
        public Builder enabledWhen(Supplier<Boolean> enabled) {
            this.enabled = enabled;
            return this;
        }

        public VeilRenderType build() {
            if (this.format == null) {
                throw new IllegalStateException("Vertex format must be set for render type " + this.name);
            }
            // The pipeline only carries the vertex format and mode for the vanilla buffer code. It is never compiled.
            RenderPipeline pipeline = RenderPipeline.builder()
                    .withLocation(Identifier.parse(this.name.contains(":") ? this.name : "veil:" + this.name).withPrefix("veil_render_type/"))
                    .withVertexShader("core/position")
                    .withFragmentShader("core/position")
                    .withVertexFormat(this.format, this.mode)
                    .withCull(this.state.cull)
                    .build();
            RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline).bufferSize(this.bufferSize);
            if (this.sortOnUpload) {
                setup.sortOnUpload();
            }
            if (this.affectsCrumbling) {
                setup.affectsCrumbling();
            }
            return new VeilRenderType(this.name, setup.createRenderSetup(), this.shader, Map.copyOf(this.textures), this.state.copy(),
                    this.patchVertices, this.lightmap, this.overlay, this.setup, this.enabled);
        }
    }
}
