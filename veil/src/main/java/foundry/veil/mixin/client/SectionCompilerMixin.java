package foundry.veil.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.rendertype.VeilBlockLayers;
import foundry.veil.impl.client.render.blocklayer.VeilSectionLayerCompiler;
import foundry.veil.impl.client.render.blocklayer.VeilSectionLayerHolder;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {

    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    @Inject(method = "compile", at = @At("HEAD"))
    private void veil$startCompile(CallbackInfoReturnable<SectionCompiler.Results> cir) {
        VeilSectionLayerCompiler.reset();
    }

    @Inject(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/List;)V", shift = At.Shift.AFTER))
    private void veil$renderExtraLayers(SectionPos sectionPos, RenderSectionRegion region, com.mojang.blaze3d.vertex.VertexSorting vertexSorting, net.minecraft.client.renderer.SectionBufferBuilderPack pack, CallbackInfoReturnable<SectionCompiler.Results> cir,
                                        @Local(ordinal = 2) BlockPos pos, @Local BlockState state, @Local PoseStack poseStack, @Local List<BlockModelPart> parts) {
        if (VeilBlockLayers.hasExtraLayers()) {
            VeilSectionLayerCompiler.renderBlock(this.blockRenderer, state, pos, region, poseStack, parts);
        }
    }

    @Inject(method = "compile", at = @At("RETURN"))
    private void veil$finishCompile(SectionPos sectionPos, RenderSectionRegion region, com.mojang.blaze3d.vertex.VertexSorting vertexSorting, net.minecraft.client.renderer.SectionBufferBuilderPack pack, CallbackInfoReturnable<SectionCompiler.Results> cir) {
        ((VeilSectionLayerHolder) (Object) cir.getReturnValue()).veil$setLayerData(VeilSectionLayerCompiler.finish());
    }
}
