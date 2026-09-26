package foundry.veil.api.client.render;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.impl.client.render.VeilGlStateSync;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import foundry.veil.Veil;
import foundry.veil.api.client.render.shader.block.ShaderBlock;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.impl.client.render.VeilImmediateRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.*;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL20C.glGetUniformLocation;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31C.glBindBufferRange;

/**
 * Static entry point to the Veil renderer.
 */
public final class VeilRenderSystem {

    private static final Matrix4f PROJECTION_MATRIX = new Matrix4f();
    private static final float[] SHADER_COLOR = {1.0F, 1.0F, 1.0F, 1.0F};
    private static final int[] SHADER_TEXTURES = new int[12];
    private static final VeilDrawState DRAW_STATE = new VeilDrawState();
    private static final Map<String, ShaderBlock<?>> BOUND_BLOCKS = new HashMap<>();
    private static final Map<RenderPipeline, VanillaProgramUniforms> VANILLA_UNIFORMS = new IdentityHashMap<>();

    @Nullable
    private static VeilRenderer renderer;
    @Nullable
    private static ShaderProgram shader;
    @Nullable
    private static CullFrustum cullingFrustum;

    private VeilRenderSystem() {
    }

    @ApiStatus.Internal
    public static void init() {
        RenderSystem.assertOnRenderThread();
        renderer = new VeilRenderer();
        Veil.LOGGER.info("Veil renderer initialized (tessellation: {}, DSA: {})", tessellationSupported(), directStateAccessSupported());
    }

    public static boolean isRendererAvailable() {
        return renderer != null;
    }

    /**
     * @return The Veil renderer instance
     */
    public static VeilRenderer renderer() {
        return Objects.requireNonNull(renderer, "Veil renderer is not available yet");
    }

    public static GLCapabilities glCapabilities() {
        return GL.getCapabilities();
    }

    public static boolean tessellationSupported() {
        GLCapabilities caps = glCapabilities();
        return caps.OpenGL40 || caps.GL_ARB_tessellation_shader;
    }

    public static boolean directStateAccessSupported() {
        GLCapabilities caps = glCapabilities();
        return caps.OpenGL45 || caps.GL_ARB_direct_state_access;
    }

    public static boolean separateShaderObjectsSupported() {
        GLCapabilities caps = glCapabilities();
        return caps.OpenGL41 || caps.GL_ARB_separate_shader_objects;
    }

    /**
     * @return The direct state access implementation of the vanilla GL backend
     */
    public static DirectStateAccess directStateAccess() {
        return ((GlDevice) RenderSystem.getDevice()).directStateAccess();
    }

    /**
     * ImGui is provided by the optional <code>imguimc</code> mod, which does not exist for 1.21.11.
     */
    public static boolean hasImGui() {
        return false;
    }

    /**
     * Marks every chunk section for recompilation.
     */
    public static void rebuildChunks() {
        Minecraft.getInstance().levelRenderer.allChanged();
    }

    /**
     * @return An executor that runs tasks on the render thread
     */
    public static Executor renderThreadExecutor() {
        return task -> {
            if (RenderSystem.isOnRenderThread()) {
                task.run();
            } else {
                Minecraft.getInstance().execute(task);
            }
        };
    }

    public static @Nullable CullFrustum getCullingFrustum() {
        return cullingFrustum;
    }

    @ApiStatus.Internal
    public static void setCullingFrustum(Frustum frustum) {
        cullingFrustum = CullFrustum.of(frustum);
    }

    // ---- Projection -------------------------------------------------------------------------------------------------

    /**
     * @return The projection matrix vanilla currently renders with
     */
    public static Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    @ApiStatus.Internal
    public static void setProjectionMatrix(Matrix4fc projection) {
        PROJECTION_MATRIX.set(projection);
    }

    // ---- Shaders ----------------------------------------------------------------------------------------------------

    /**
     * Makes the specified Veil program the current shader and sets its classic default uniforms.
     *
     * @return The program or <code>null</code> if it does not exist
     */
    public static @Nullable ShaderProgram setShader(Identifier name) {
        ShaderProgram program = renderer().getShaderManager().getShader(name);
        return setShader(program);
    }

    public static @Nullable ShaderProgram setShader(@Nullable ShaderProgram program) {
        shader = program;
        if (program != null) {
            program.setDefaultUniforms(RenderSystem.getModelViewMatrix(), PROJECTION_MATRIX);
        }
        return program;
    }

    public static @Nullable ShaderProgram getShader() {
        return shader;
    }

    public static void clearShader() {
        shader = null;
    }

    @ApiStatus.Internal
    public static void setBoundShader(@Nullable ShaderProgram program) {
        // Veil programs are only bound for the duration of a single draw
    }

    public static float[] getShaderColor() {
        return SHADER_COLOR;
    }

    public static void setShaderColor(float red, float green, float blue, float alpha) {
        SHADER_COLOR[0] = red;
        SHADER_COLOR[1] = green;
        SHADER_COLOR[2] = blue;
        SHADER_COLOR[3] = alpha;
    }

    /**
     * Sets the texture sampled by <code>Sampler&lt;unit&gt;</code> in Veil programs.
     */
    public static void setShaderTexture(int unit, Identifier texture) {
        AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
        SHADER_TEXTURES[unit] = getTextureId(abstractTexture.getTextureView());
    }

    public static void setShaderTexture(int unit, int textureId) {
        SHADER_TEXTURES[unit] = textureId;
    }

    public static void setShaderTexture(int unit, GpuTextureView texture) {
        SHADER_TEXTURES[unit] = getTextureId(texture);
    }

    /**
     * Resolves the texture of the classic vanilla samplers (<code>Sampler0</code> - <code>Sampler11</code>).
     */
    @ApiStatus.Internal
    public static int getStandardSampler(String name) {
        if (name.startsWith("Sampler")) {
            try {
                int unit = Integer.parseInt(name.substring("Sampler".length()));
                if (unit >= 0 && unit < SHADER_TEXTURES.length) {
                    if (SHADER_TEXTURES[unit] != 0) {
                        return SHADER_TEXTURES[unit];
                    }
                    Minecraft minecraft = Minecraft.getInstance();
                    if (unit == 1) {
                        return getTextureId(minecraft.gameRenderer.overlayTexture().getTextureView());
                    }
                    if (unit == 2) {
                        return getTextureId(minecraft.gameRenderer.lightTexture().getTextureView());
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Clears every texture set through {@link #setShaderTexture(int, int)}.
     */
    public static void resetShaderTextures() {
        Arrays.fill(SHADER_TEXTURES, 0);
    }

    public static int getTextureId(GpuTextureView view) {
        return ((GlTextureView) view).texture().glId();
    }

    public static int getTextureId(GlTexture texture) {
        return texture.glId();
    }

    // ---- Draw state -------------------------------------------------------------------------------------------------

    /**
     * @return The mutable draw state used by {@link #drawWithShader(MeshData)}
     */
    public static VeilDrawState drawState() {
        return DRAW_STATE;
    }

    /**
     * Draws the mesh with the current shader (see {@link #setShader(Identifier)}) into the currently bound Veil
     * framebuffer, or the main render target if none is bound.
     */
    public static void drawWithShader(MeshData meshData) {
        ShaderProgram program = shader;
        if (program == null) {
            meshData.close();
            return;
        }
        VeilImmediateRenderer.draw(meshData, program, DRAW_STATE, 0);
    }

    public static void drawWithShader(MeshData meshData, ShaderProgram program, VeilDrawState state) {
        VeilImmediateRenderer.draw(meshData, program, state, 0);
    }

    public static void drawPatches(MeshData meshData, ShaderProgram program, VeilDrawState state, int patchVertices) {
        VeilImmediateRenderer.draw(meshData, program, state, patchVertices);
    }

    /**
     * Draws a full screen triangle with the specified program.
     */
    public static void drawScreenQuad(ShaderProgram program, VeilDrawState state) {
        VeilImmediateRenderer.drawScreenQuad(program, state);
    }

    // ---- Shader blocks ----------------------------------------------------------------------------------------------

    /**
     * Makes the specified block available to every Veil program declaring a block with the same name.
     */
    public static void bind(CharSequence name, ShaderBlock<?> block) {
        BOUND_BLOCKS.put(name.toString(), block);
    }

    public static void unbind(ShaderBlock<?> block) {
        BOUND_BLOCKS.values().removeIf(value -> value == block);
    }

    @ApiStatus.Internal
    public static boolean isBoundBlockName(String name) {
        return BOUND_BLOCKS.containsKey(name);
    }

    @ApiStatus.Internal
    public static void bindShaderBlock(String name, int binding) {
        ShaderBlock<?> block = BOUND_BLOCKS.get(name);
        if (block != null) {
            block.bind(binding);
        }
    }

    // ---- Vanilla programs -------------------------------------------------------------------------------------------

    /**
     * Gets write access to classic uniforms injected into a vanilla pipeline by a shader pre-processor.
     *
     * @param pipeline The vanilla pipeline
     * @param name     The name of the uniform
     */
    public static ShaderUniformAccess getVanillaUniform(RenderPipeline pipeline, String name) {
        return VANILLA_UNIFORMS.computeIfAbsent(pipeline, VanillaProgramUniforms::new).get(name);
    }

    /**
     * @return The OpenGL program id vanilla uses for the specified pipeline, compiling it if needed
     */
    public static int getVanillaProgram(RenderPipeline pipeline) {
        return ((GlRenderPipeline) RenderSystem.getDevice().precompilePipeline(pipeline)).program().getProgramId();
    }

    @ApiStatus.Internal
    public static void clearVanillaProgramCache() {
        VANILLA_UNIFORMS.clear();
    }

    /**
     * Discards every compiled vanilla pipeline so they are recompiled (running all vanilla shader pre-processors again)
     * the next time they are used. Call this when the state a {@link foundry.veil.api.client.render.shader.processor.ShaderPreProcessor}
     * depends on changes.
     */
    public static void reloadVanillaShaders() {
        RenderSystem.assertOnRenderThread();
        RenderSystem.getDevice().clearPipelineCache();
        VeilGlStateSync.invalidateVanillaCaches();
        clearVanillaProgramCache();
    }

    private static final class VanillaProgramUniforms {

        private final RenderPipeline pipeline;
        private final Map<String, ShaderUniform> uniforms = new HashMap<>();
        private int program = -1;

        private VanillaProgramUniforms(RenderPipeline pipeline) {
            this.pipeline = pipeline;
        }

        private ShaderUniformAccess get(String name) {
            int currentProgram = getVanillaProgram(this.pipeline);
            if (currentProgram != this.program) {
                this.program = currentProgram;
                this.uniforms.clear();
            }
            if (currentProgram <= 0) {
                return ShaderUniform.INVALID;
            }
            return this.uniforms.computeIfAbsent(name, key -> {
                int location = glGetUniformLocation(currentProgram, key);
                return location == -1 ? ShaderUniform.INVALID : new ShaderUniform(currentProgram, location, key);
            });
        }
    }
}
