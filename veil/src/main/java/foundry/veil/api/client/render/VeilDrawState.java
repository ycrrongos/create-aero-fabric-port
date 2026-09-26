package foundry.veil.api.client.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL32C;

import static org.lwjgl.opengl.GL11C.*;

/**
 * Fixed-function state used by raw Veil draws. This replaces the global state setters that were removed from
 * {@code RenderSystem} in 1.21.5+ (depth test, blending, culling, polygon offset...).
 */
public final class VeilDrawState {

    public boolean depthTest = true;
    public int depthFunc = GL_LEQUAL;
    public boolean depthMask = true;
    public boolean colorMaskRed = true;
    public boolean colorMaskGreen = true;
    public boolean colorMaskBlue = true;
    public boolean colorMaskAlpha = true;
    public boolean cull = true;
    public int cullFace = GL_BACK;
    @Nullable
    public int[] blendFunc = null;
    public boolean polygonOffset;
    public float polygonOffsetFactor;
    public float polygonOffsetUnits;
    public boolean depthClamp;

    public VeilDrawState copy() {
        VeilDrawState state = new VeilDrawState();
        state.set(this);
        return state;
    }

    public VeilDrawState set(VeilDrawState other) {
        this.depthTest = other.depthTest;
        this.depthFunc = other.depthFunc;
        this.depthMask = other.depthMask;
        this.colorMaskRed = other.colorMaskRed;
        this.colorMaskGreen = other.colorMaskGreen;
        this.colorMaskBlue = other.colorMaskBlue;
        this.colorMaskAlpha = other.colorMaskAlpha;
        this.cull = other.cull;
        this.cullFace = other.cullFace;
        this.blendFunc = other.blendFunc != null ? other.blendFunc.clone() : null;
        this.polygonOffset = other.polygonOffset;
        this.polygonOffsetFactor = other.polygonOffsetFactor;
        this.polygonOffsetUnits = other.polygonOffsetUnits;
        this.depthClamp = other.depthClamp;
        return this;
    }

    /**
     * Resets to the default opaque state (depth test and write, back-face culling, no blending).
     */
    public VeilDrawState reset() {
        return this.set(new VeilDrawState());
    }

    public VeilDrawState enableDepthTest() {
        this.depthTest = true;
        return this;
    }

    public VeilDrawState disableDepthTest() {
        this.depthTest = false;
        return this;
    }

    public VeilDrawState depthFunc(int func) {
        this.depthFunc = func;
        return this;
    }

    public VeilDrawState depthMask(boolean mask) {
        this.depthMask = mask;
        return this;
    }

    public VeilDrawState colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        this.colorMaskRed = red;
        this.colorMaskGreen = green;
        this.colorMaskBlue = blue;
        this.colorMaskAlpha = alpha;
        return this;
    }

    public VeilDrawState enableCull() {
        this.cull = true;
        return this;
    }

    public VeilDrawState disableCull() {
        this.cull = false;
        return this;
    }

    public VeilDrawState cullFace(int face) {
        this.cullFace = face;
        return this;
    }

    public VeilDrawState defaultBlend() {
        return this.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    }

    public VeilDrawState additiveBlend() {
        return this.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ONE);
    }

    public VeilDrawState blendFuncSeparate(int srcColor, int dstColor, int srcAlpha, int dstAlpha) {
        this.blendFunc = new int[]{srcColor, dstColor, srcAlpha, dstAlpha};
        return this;
    }

    public VeilDrawState disableBlend() {
        this.blendFunc = null;
        return this;
    }

    public VeilDrawState polygonOffset(float factor, float units) {
        this.polygonOffset = factor != 0.0F || units != 0.0F;
        this.polygonOffsetFactor = factor;
        this.polygonOffsetUnits = units;
        return this;
    }

    public VeilDrawState depthClamp(boolean depthClamp) {
        this.depthClamp = depthClamp;
        return this;
    }

    /**
     * Applies this state to OpenGL through the vanilla state manager so its caches stay valid.
     */
    public void apply() {
        if (this.depthTest) {
            GlStateManager._enableDepthTest();
            GlStateManager._depthFunc(this.depthFunc);
        } else {
            GlStateManager._disableDepthTest();
        }
        GlStateManager._depthMask(this.depthMask);
        GlStateManager._colorMask(this.colorMaskRed, this.colorMaskGreen, this.colorMaskBlue, this.colorMaskAlpha);
        if (this.cull) {
            GlStateManager._enableCull();
            GL11C.glCullFace(this.cullFace);
        } else {
            GlStateManager._disableCull();
        }
        if (this.blendFunc != null) {
            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(this.blendFunc[0], this.blendFunc[1], this.blendFunc[2], this.blendFunc[3]);
        } else {
            GlStateManager._disableBlend();
        }
        if (this.polygonOffset) {
            GlStateManager._enablePolygonOffset();
            GlStateManager._polygonOffset(this.polygonOffsetFactor, this.polygonOffsetUnits);
        } else {
            GlStateManager._disablePolygonOffset();
        }
        if (this.depthClamp) {
            GL11C.glEnable(GL32C.GL_DEPTH_CLAMP);
        } else {
            GL11C.glDisable(GL32C.GL_DEPTH_CLAMP);
        }
        GlStateManager._polygonMode(GL_FRONT_AND_BACK, GL_FILL);
        GlStateManager._disableColorLogicOp();
    }
}
