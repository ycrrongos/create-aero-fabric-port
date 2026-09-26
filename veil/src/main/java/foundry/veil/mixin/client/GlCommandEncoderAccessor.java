package foundry.veil.mixin.client;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlCommandEncoder.class)
public interface GlCommandEncoderAccessor {

    @Accessor("lastProgram")
    void veil$setLastProgram(GlProgram program);

    @Accessor("lastPipeline")
    void veil$setLastPipeline(RenderPipeline pipeline);

    @Accessor("inRenderPass")
    boolean veil$isInRenderPass();
}
