package foundry.veil.api.client.render.framebuffer;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

/**
 * A texture attached to an {@link AdvancedFbo}. Backed by a vanilla {@link GpuTexture} so it can be used both by Veil
 * programs and vanilla render passes.
 */
public final class AdvancedFboTextureAttachment {

    private final GpuTexture texture;
    private final GpuTextureView view;
    private final boolean depth;

    AdvancedFboTextureAttachment(GpuTexture texture, GpuTextureView view, boolean depth) {
        this.texture = texture;
        this.view = view;
        this.depth = depth;
    }

    /**
     * @return The OpenGL texture id of this attachment
     */
    public int getId() {
        return ((GlTexture) this.texture).glId();
    }

    public GpuTexture getTexture() {
        return this.texture;
    }

    public GpuTextureView getTextureView() {
        return this.view;
    }

    public boolean isDepth() {
        return this.depth;
    }

    public int getWidth() {
        return this.texture.getWidth(0);
    }

    public int getHeight() {
        return this.texture.getHeight(0);
    }
}
