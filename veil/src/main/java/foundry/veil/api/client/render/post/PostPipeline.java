package foundry.veil.api.client.render.post;

import foundry.veil.api.client.render.VeilDrawState;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A post-processing operation. Pipelines are loaded from <code>pinwheel/post</code>.
 */
@FunctionalInterface
public interface PostPipeline {

    /**
     * Runs this pipeline.
     */
    void apply(Context context);

    /**
     * State shared between all stages of a pipeline run.
     */
    interface Context {

        /**
         * @return The framebuffer with the specified name, or <code>null</code> if it does not exist
         */
        @Nullable
        AdvancedFbo getFramebuffer(Identifier name);

        /**
         * @return The framebuffer with the specified name, or the post framebuffer if it does not exist
         */
        AdvancedFbo getFramebufferOrDraw(Identifier name);

        /**
         * Makes a framebuffer available under the specified name for the next pipeline runs.
         */
        void setFramebuffer(Identifier name, AdvancedFbo framebuffer);

        /**
         * Sets a texture that every stage binds to the sampler with the specified name.
         */
        void setTexture(CharSequence name, int textureId);

        /**
         * Binds all context textures to the specified program.
         */
        void applySamplers(ShaderProgram program);

        /**
         * @return The fixed-function state stages draw with
         */
        VeilDrawState drawState();
    }
}
