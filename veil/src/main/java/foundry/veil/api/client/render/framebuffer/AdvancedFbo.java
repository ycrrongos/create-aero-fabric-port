package foundry.veil.api.client.render.framebuffer;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30C.*;

/**
 * An OpenGL framebuffer with any number of color attachments and an optional depth attachment.
 * <p>
 * Attachments are vanilla {@link GpuTexture}s, so the framebuffer can be the target of raw Veil draws (through
 * {@link #bind(boolean)}) as well as vanilla {@link net.minecraft.client.renderer.rendertype.RenderType} draws, which are
 * redirected through {@link RenderSystem#outputColorTextureOverride}.
 */
public class AdvancedFbo implements NativeResource {

    private static final MainFbo MAIN = new MainFbo();
    @Nullable
    private static AdvancedFbo bound;

    private final int width;
    private final int height;
    private final List<TextureFormat> colorFormats;
    private final boolean hasDepth;
    private final String label;
    private final List<AdvancedFboTextureAttachment> colorAttachments = new ArrayList<>();
    @Nullable
    private AdvancedFboTextureAttachment depthAttachment;
    private int id = -1;

    protected AdvancedFbo(String label, int width, int height, List<TextureFormat> colorFormats, boolean hasDepth) {
        this.label = label;
        this.width = width;
        this.height = height;
        this.colorFormats = colorFormats;
        this.hasDepth = hasDepth;
    }

    public static Builder withSize(int width, int height) {
        return new Builder(width, height);
    }

    /**
     * @return A view of the vanilla main framebuffer
     */
    public static AdvancedFbo getMainFramebuffer() {
        return MAIN;
    }

    /**
     * @return The framebuffer currently bound through Veil, or <code>null</code> if the main target is active
     */
    public static @Nullable AdvancedFbo getBound() {
        return bound;
    }

    /**
     * Creates the GPU resources of this framebuffer. Called automatically by {@link Builder#build(boolean)}.
     */
    public void create() {
        if (this.id != -1) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        GpuDevice device = RenderSystem.getDevice();
        int usage = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST;
        for (int i = 0; i < this.colorFormats.size(); i++) {
            int index = i;
            GpuTexture texture = device.createTexture(() -> this.label + " / Color " + index, usage, this.colorFormats.get(i), this.width, this.height, 1, 1);
            GpuTextureView view = device.createTextureView(texture);
            this.colorAttachments.add(new AdvancedFboTextureAttachment(texture, view, false));
        }
        if (this.hasDepth) {
            GpuTexture texture = device.createTexture(() -> this.label + " / Depth", usage, TextureFormat.DEPTH32, this.width, this.height, 1, 1);
            GpuTextureView view = device.createTextureView(texture);
            this.depthAttachment = new AdvancedFboTextureAttachment(texture, view, true);
        }

        this.id = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, this.id);
        for (int i = 0; i < this.colorAttachments.size(); i++) {
            GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, this.colorAttachments.get(i).getId(), 0);
        }
        if (this.depthAttachment != null) {
            GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthAttachment.getId(), 0);
        }
        this.resetDrawBuffers();
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer " + this.label + " is incomplete: 0x" + Integer.toHexString(status));
        }
    }

    /**
     * Enables drawing into every color attachment.
     */
    public void resetDrawBuffers() {
        if (this.colorAttachments.isEmpty()) {
            glDrawBuffer(GL_NONE);
            glReadBuffer(GL_NONE);
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffers = stack.mallocInt(this.colorAttachments.size());
            for (int i = 0; i < this.colorAttachments.size(); i++) {
                buffers.put(i, GL_COLOR_ATTACHMENT0 + i);
            }
            glDrawBuffers(buffers);
        }
    }

    /**
     * Binds this framebuffer as the destination of all Veil and vanilla draws until {@link #unbind()} is called.
     *
     * @param setViewport Whether to set the viewport to the size of this framebuffer
     */
    public void bind(boolean setViewport) {
        this.create();
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, this.getId());
        if (setViewport) {
            GlStateManager._viewport(0, 0, this.getWidth(), this.getHeight());
        }
        bound = this == MAIN ? null : this;
        RenderSystem.outputColorTextureOverride = bound != null && !this.colorAttachments.isEmpty() ? this.colorAttachments.getFirst().getTextureView() : null;
        RenderSystem.outputDepthTextureOverride = bound != null && this.depthAttachment != null ? this.depthAttachment.getTextureView() : null;
    }

    /**
     * Binds this framebuffer as the read framebuffer.
     */
    public void bindRead() {
        this.create();
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.getId());
    }

    /**
     * Restores the main framebuffer as the target of all draws.
     */
    public static void unbind() {
        MAIN.bind(true);
    }

    public void clear() {
        this.clear(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, this.getClearMask());
    }

    public void clear(int clearMask) {
        this.clear(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, clearMask);
    }

    public void clear(float red, float green, float blue, float alpha, int clearMask) {
        this.clear(red, green, blue, alpha, 1.0F, clearMask);
    }

    public void clear(float red, float green, float blue, float alpha, float depth, int clearMask) {
        this.create();
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, this.getId());
        GlStateManager._disableScissorTest();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._depthMask(true);
        glClearColor(red, green, blue, alpha);
        glClearDepth(depth);
        GlStateManager._clear(clearMask & this.getClearMask());
        AdvancedFbo current = bound != null ? bound : MAIN;
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, current.getId());
    }

    /**
     * @return The mask of all buffers present in this framebuffer
     */
    public int getClearMask() {
        int mask = 0;
        if (!this.colorAttachments.isEmpty() || this.colorFormats.size() > 0) {
            mask |= GL_COLOR_BUFFER_BIT;
        }
        if (this.hasDepth) {
            mask |= GL_DEPTH_BUFFER_BIT;
        }
        return mask;
    }

    /**
     * Copies the contents of this framebuffer into the target framebuffer.
     */
    public void resolveToAdvancedFbo(AdvancedFbo target, int mask, int filtering) {
        this.create();
        target.create();
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.getId());
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, target.getId());
        glBlitFramebuffer(0, 0, this.getWidth(), this.getHeight(), 0, 0, target.getWidth(), target.getHeight(), mask, filtering);
        AdvancedFbo current = bound != null ? bound : MAIN;
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, current.getId());
    }

    public void resolveToAdvancedFbo(AdvancedFbo target) {
        this.resolveToAdvancedFbo(target, this.getClearMask() & target.getClearMask(), GL_NEAREST);
    }

    public int getId() {
        return this.id;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getColorAttachments() {
        return this.colorAttachments.size();
    }

    public boolean isColorTextureAttachment(int index) {
        return index >= 0 && index < this.colorAttachments.size();
    }

    public AdvancedFboTextureAttachment getColorTextureAttachment(int index) {
        this.create();
        return this.colorAttachments.get(index);
    }

    public boolean isDepthTextureAttachment() {
        return this.hasDepth;
    }

    public AdvancedFboTextureAttachment getDepthTextureAttachment() {
        this.create();
        if (this.depthAttachment == null) {
            throw new IllegalStateException("Framebuffer " + this.label + " has no depth attachment");
        }
        return this.depthAttachment;
    }

    @Override
    public void free() {
        if (bound == this) {
            unbind();
        }
        if (this.id != -1) {
            GlStateManager._glDeleteFramebuffers(this.id);
            this.id = -1;
        }
        for (AdvancedFboTextureAttachment attachment : this.colorAttachments) {
            attachment.getTextureView().close();
            attachment.getTexture().close();
        }
        this.colorAttachments.clear();
        if (this.depthAttachment != null) {
            this.depthAttachment.getTextureView().close();
            this.depthAttachment.getTexture().close();
            this.depthAttachment = null;
        }
    }

    @Override
    public String toString() {
        return "AdvancedFbo[" + this.label + ", " + this.width + "x" + this.height + "]";
    }

    /**
     * Builds framebuffers.
     */
    public static class Builder {

        private final int width;
        private final int height;
        private final List<TextureFormat> colorFormats = new ArrayList<>();
        private boolean depth;
        private String label = "Veil Framebuffer";

        private Builder(int width, int height) {
            this.width = Math.max(1, width);
            this.height = Math.max(1, height);
        }

        public Builder setName(String label) {
            this.label = label;
            return this;
        }

        public Builder addColorTextureBuffer() {
            return this.addColorTextureBuffer(TextureFormat.RGBA8);
        }

        public Builder addColorTextureBuffer(TextureFormat format) {
            this.colorFormats.add(format);
            return this;
        }

        public Builder setDepthTextureBuffer() {
            this.depth = true;
            return this;
        }

        public AdvancedFbo build(boolean create) {
            AdvancedFbo fbo = new AdvancedFbo(this.label, this.width, this.height, List.copyOf(this.colorFormats), this.depth);
            if (create) {
                fbo.create();
            }
            return fbo;
        }
    }

    /**
     * Adapter over the vanilla main render target.
     */
    private static final class MainFbo extends AdvancedFbo {

        private MainFbo() {
            super("Main", 1, 1, List.of(TextureFormat.RGBA8), true);
        }

        private static RenderTarget target() {
            return Minecraft.getInstance().getMainRenderTarget();
        }

        @Override
        public void create() {
        }

        @Override
        public void bind(boolean setViewport) {
            RenderTarget target = target();
            GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, this.getId());
            if (setViewport) {
                GlStateManager._viewport(0, 0, target.width, target.height);
            }
            bound = null;
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
        }

        @Override
        public int getId() {
            RenderTarget target = target();
            GpuTexture color = target.getColorTexture();
            return ((GlTexture) color).getFbo(VeilRenderSystem.directStateAccess(), target.getDepthTexture());
        }

        @Override
        public int getWidth() {
            return target().width;
        }

        @Override
        public int getHeight() {
            return target().height;
        }

        @Override
        public int getColorAttachments() {
            return 1;
        }

        @Override
        public boolean isColorTextureAttachment(int index) {
            return index == 0;
        }

        @Override
        public AdvancedFboTextureAttachment getColorTextureAttachment(int index) {
            RenderTarget target = target();
            return new AdvancedFboTextureAttachment(target.getColorTexture(), target.getColorTextureView(), false);
        }

        @Override
        public boolean isDepthTextureAttachment() {
            return target().getDepthTexture() != null;
        }

        @Override
        public AdvancedFboTextureAttachment getDepthTextureAttachment() {
            RenderTarget target = target();
            return new AdvancedFboTextureAttachment(target.getDepthTexture(), target.getDepthTextureView(), true);
        }

        @Override
        public void free() {
        }
    }
}
