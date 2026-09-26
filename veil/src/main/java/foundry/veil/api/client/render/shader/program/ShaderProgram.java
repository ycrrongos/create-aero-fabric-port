package foundry.veil.api.client.render.shader.program;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFboTextureAttachment;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.client.render.shader.texture.ShaderTextureSource;
import foundry.veil.impl.client.render.VeilFogState;
import foundry.veil.impl.client.render.VeilGlStateSync;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.*;

/**
 * A linked Veil shader program.
 * <p>
 * Programs support classic uniforms (set through {@link #getUniform(CharSequence)}), named samplers, and the vanilla
 * 1.21.11 uniform blocks (<code>Projection</code>, <code>Fog</code>, <code>Globals</code>, <code>Lighting</code>,
 * <code>DynamicTransforms</code>), which are bound automatically from the current vanilla render state.
 */
public final class ShaderProgram implements NativeResource {

    private static final Set<String> VANILLA_BLOCKS = Set.of("Projection", "Fog", "Globals", "Lighting", "DynamicTransforms");

    private final Identifier name;
    @Nullable
    private final ProgramDefinition definition;
    private final int program;
    private final boolean tessellation;
    private final Map<String, ShaderUniform> uniforms = new HashMap<>();
    private final List<String> samplerNames = new ArrayList<>();
    private final Map<String, Integer> samplerLocations = new HashMap<>();
    private final Map<String, Integer> manualSamplers = new HashMap<>();
    private final Object2IntMap<String> blockBindings = new Object2IntArrayMap<>();
    @Nullable
    private GpuBufferSlice dynamicTransforms;

    ShaderProgram(Identifier name, @Nullable ProgramDefinition definition, int program, boolean tessellation) {
        this.name = name;
        this.definition = definition;
        this.program = program;
        this.tessellation = tessellation;
        this.introspect();
    }

    /**
     * Creates a program object from linked OpenGL program id.
     */
    public static ShaderProgram create(Identifier name, @Nullable ProgramDefinition definition, int program, boolean tessellation) {
        return new ShaderProgram(name, definition, program, tessellation);
    }

    private void introspect() {
        int count = glGetProgrami(this.program, GL_ACTIVE_UNIFORMS);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer size = stack.mallocInt(1);
            IntBuffer type = stack.mallocInt(1);
            for (int i = 0; i < count; i++) {
                String uniformName = glGetActiveUniform(this.program, i, size, type);
                if (isSampler(type.get(0))) {
                    int location = glGetUniformLocation(this.program, uniformName);
                    if (location != -1) {
                        this.samplerNames.add(uniformName);
                        this.samplerLocations.put(uniformName, location);
                    }
                }
            }
        }

        int blocks = glGetProgrami(this.program, GL_ACTIVE_UNIFORM_BLOCKS);
        int binding = 0;
        for (int i = 0; i < blocks; i++) {
            String blockName = glGetActiveUniformBlockName(this.program, i);
            glUniformBlockBinding(this.program, i, binding);
            this.blockBindings.put(blockName, binding);
            if (!VANILLA_BLOCKS.contains(blockName) && !VeilRenderSystem.isBoundBlockName(blockName)) {
                Veil.LOGGER.debug("Program {} declares uniform block {} which must be bound manually", this.name, blockName);
            }
            binding++;
        }
    }

    private static boolean isSampler(int type) {
        return switch (type) {
            case GL_SAMPLER_1D, GL_SAMPLER_2D, GL_SAMPLER_3D, GL_SAMPLER_CUBE, GL_SAMPLER_2D_SHADOW, GL_SAMPLER_1D_ARRAY,
                 GL_SAMPLER_2D_ARRAY, GL_SAMPLER_BUFFER, GL_INT_SAMPLER_2D, GL_UNSIGNED_INT_SAMPLER_2D, GL_INT_SAMPLER_BUFFER,
                 GL_UNSIGNED_INT_SAMPLER_BUFFER, GL_SAMPLER_2D_ARRAY_SHADOW, GL_SAMPLER_CUBE_SHADOW -> true;
            default -> false;
        };
    }

    public Identifier getName() {
        return this.name;
    }

    public @Nullable ProgramDefinition getDefinition() {
        return this.definition;
    }

    /**
     * @return The OpenGL program id
     */
    public int getProgram() {
        return this.program;
    }

    public boolean hasTessellation() {
        return this.tessellation;
    }

    /**
     * @return The uniform with the specified name or <code>null</code> if it doesn't exist in the program
     */
    public @Nullable ShaderUniform getUniform(CharSequence name) {
        String key = name.toString();
        ShaderUniform uniform = this.uniforms.get(key);
        if (uniform == null) {
            int location = glGetUniformLocation(this.program, key);
            uniform = location == -1 ? ShaderUniform.INVALID : new ShaderUniform(this.program, location, key);
            this.uniforms.put(key, uniform);
        }
        return uniform.isValid() ? uniform : null;
    }

    /**
     * @return The uniform with the specified name. Writes to missing uniforms are ignored
     */
    public ShaderUniformAccess getUniformSafe(CharSequence name) {
        ShaderUniform uniform = this.getUniform(name);
        return uniform != null ? uniform : ShaderUniform.INVALID;
    }

    public boolean hasUniform(CharSequence name) {
        return this.getUniform(name) != null;
    }

    public boolean hasSampler(CharSequence name) {
        return this.samplerLocations.containsKey(name.toString());
    }

    public List<String> getSamplerNames() {
        return Collections.unmodifiableList(this.samplerNames);
    }

    /**
     * Sets a sampler to the specified raw OpenGL texture id until the program is re-created.
     */
    public void setSampler(CharSequence name, int textureId) {
        this.manualSamplers.put(name.toString(), textureId);
    }

    public void setSampler(CharSequence name, AbstractTexture texture) {
        this.setSampler(name, VeilRenderSystem.getTextureId(texture.getTextureView()));
    }

    public void setSampler(CharSequence name, GpuTextureView texture) {
        this.setSampler(name, VeilRenderSystem.getTextureId(texture));
    }

    public void setSampler(CharSequence name, AdvancedFboTextureAttachment attachment) {
        this.setSampler(name, attachment.getId());
    }

    public void setSampler(CharSequence name, Identifier texture) {
        this.setSampler(name, Minecraft.getInstance().getTextureManager().getTexture(texture));
    }

    public void clearSamplers() {
        this.manualSamplers.clear();
    }

    /**
     * Uses the specified dynamic transform buffer the next time this program is bound, instead of writing one from the
     * current model view matrix.
     */
    public void setDynamicTransforms(@Nullable GpuBufferSlice slice) {
        this.dynamicTransforms = slice;
    }

    /**
     * Sets the classic vanilla uniforms (<code>ModelViewMat</code>, <code>ProjMat</code>, fog and screen parameters).
     *
     * @param modelView  The model view matrix
     * @param projection The projection matrix
     */
    public void setDefaultUniforms(Matrix4fc modelView, Matrix4fc projection) {
        this.getUniformSafe("ModelViewMat").setMatrix(modelView);
        this.getUniformSafe("ProjMat").setMatrix(projection);
        ShaderUniform normal = this.getUniform("NormalMat");
        if (normal != null) {
            normal.setMatrix(new Matrix4f(modelView).normal(new Matrix3f()));
        }
        ShaderUniform inverseView = this.getUniform("IViewRotMat");
        if (inverseView != null) {
            inverseView.setMatrix(new Matrix3f(new Matrix4f(modelView)).invert());
        }
        float[] color = VeilRenderSystem.getShaderColor();
        this.getUniformSafe("ColorModulator").setVector(color[0], color[1], color[2], color[3]);

        VeilFogState fog = VeilFogState.get();
        this.getUniformSafe("FogStart").setFloat(fog.legacyStart());
        this.getUniformSafe("FogEnd").setFloat(fog.legacyEnd());
        this.getUniformSafe("FogColor").setVector(fog.red(), fog.green(), fog.blue(), fog.alpha());
        this.getUniformSafe("FogShape").setInt(fog.legacyShape());

        Minecraft minecraft = Minecraft.getInstance();
        this.getUniformSafe("ScreenSize").setVector((float) minecraft.getWindow().getWidth(), (float) minecraft.getWindow().getHeight());
        ClientLevel level = minecraft.level;
        if (level != null) {
            float gameTime = (level.getGameTime() % 24000L + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)) / 24000.0F;
            this.getUniformSafe("GameTime").setFloat(gameTime);
            ShaderUniform brightness = this.getUniform("VeilBlockFaceBrightness");
            if (brightness != null) {
                float[] values = new float[6];
                for (Direction direction : Direction.values()) {
                    values[direction.ordinal()] = level.getShade(direction, true);
                }
                brightness.setFloats(values);
            }
        }
    }

    /**
     * Binds this program, all of its samplers and all vanilla uniform blocks it declares.
     */
    public void bind() {
        VeilGlStateSync.useProgram(this.program);
        VeilRenderSystem.setBoundShader(this);

        int unit = 0;
        for (String samplerName : this.samplerNames) {
            int textureId = this.resolveSampler(samplerName);
            GlStateManager._activeTexture(GL_TEXTURE0 + unit);
            GlStateManager._bindTexture(textureId);
            VeilGlStateSync.clearSampler(unit);
            glUniform1i(this.samplerLocations.get(samplerName), unit);
            unit++;
        }
        GlStateManager._activeTexture(GL_TEXTURE0);

        for (Object2IntMap.Entry<String> entry : this.blockBindings.object2IntEntrySet()) {
            GpuBufferSlice slice = this.resolveBlock(entry.getKey());
            if (slice != null) {
                glBindBufferRange(GL_UNIFORM_BUFFER, entry.getIntValue(), ((GlBuffer) slice.buffer()).handle, slice.offset(), slice.length());
            } else {
                VeilRenderSystem.bindShaderBlock(entry.getKey(), entry.getIntValue());
            }
        }
    }

    private int resolveSampler(String samplerName) {
        Integer manual = this.manualSamplers.get(samplerName);
        if (manual != null) {
            return manual;
        }
        if (this.definition != null) {
            ShaderTextureSource source = this.definition.textures().get(samplerName);
            if (source != null) {
                return source.getId();
            }
        }
        return VeilRenderSystem.getStandardSampler(samplerName);
    }

    private @Nullable GpuBufferSlice resolveBlock(String blockName) {
        return switch (blockName) {
            case "Projection" -> RenderSystem.getProjectionMatrixBuffer();
            case "Fog" -> RenderSystem.getShaderFog();
            case "Globals" -> RenderSystem.getGlobalSettingsUniform() != null ? RenderSystem.getGlobalSettingsUniform().slice() : null;
            case "Lighting" -> RenderSystem.getShaderLights();
            case "DynamicTransforms" -> {
                if (this.dynamicTransforms != null) {
                    yield this.dynamicTransforms;
                }
                float[] color = VeilRenderSystem.getShaderColor();
                yield RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(),
                        new org.joml.Vector4f(color[0], color[1], color[2], color[3]), new org.joml.Vector3f(), new Matrix4f());
            }
            default -> null;
        };
    }

    /**
     * Unbinds any Veil program.
     */
    public static void unbind() {
        VeilGlStateSync.useProgram(0);
        VeilRenderSystem.setBoundShader(null);
    }

    @Override
    public void free() {
        glDeleteProgram(this.program);
        this.uniforms.clear();
    }

    @Override
    public String toString() {
        return "ShaderProgram[" + this.name + "]";
    }
}
