package foundry.veil.impl.client.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.mixin.client.GlCommandEncoderAccessor;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;

/**
 * Keeps the 1.21.11 GL backend caches coherent when Veil issues raw OpenGL calls.
 * <p>
 * The vanilla {@code GlCommandEncoder} caches the last bound program and pipeline. Any time Veil binds its own program the
 * cached values become stale, so they are invalidated here. State that is never touched by vanilla pipelines (cull face,
 * depth clamp, patch vertices) is always restored to the vanilla defaults.
 */
@ApiStatus.Internal
public final class VeilGlStateSync {

    private VeilGlStateSync() {
    }

    /**
     * Marks the vanilla program/pipeline caches as dirty so the next vanilla draw rebinds everything it needs.
     */
    public static void invalidateVanillaCaches() {
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        if (encoder instanceof GlCommandEncoderAccessor accessor) {
            accessor.veil$setLastProgram(null);
            accessor.veil$setLastPipeline(null);
        }
    }

    /**
     * Binds the specified program and invalidates the vanilla program cache.
     */
    public static void useProgram(int program) {
        GlStateManager._glUseProgram(program);
        invalidateVanillaCaches();
    }

    /**
     * Restores GL state that vanilla assumes is always at its default value.
     */
    public static void restoreNonPipelineState() {
        GL11C.glCullFace(GL11C.GL_BACK);
        GL11C.glDisable(GL32C.GL_DEPTH_CLAMP);
        GlStateManager._disableScissorTest();
    }

    /**
     * Unbinds any sampler object from the specified texture unit so the texture's own parameters are used.
     */
    public static void clearSampler(int unit) {
        GL33C.glBindSampler(unit, 0);
    }
}
