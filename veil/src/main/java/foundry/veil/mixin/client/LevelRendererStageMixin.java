package foundry.veil.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.impl.client.render.VeilLevelRenderStages;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererStageMixin {

    @Shadow
    private int ticks;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void veil$beginLevel(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix, GpuBufferSlice shaderFog, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        VeilRenderSystem.setProjectionMatrix(projectionMatrix);
        net.minecraft.world.phys.Vec3 pos = camera.position();
        VeilRenderSystem.renderer().getCameraMatrices().update(projectionMatrix, frustumMatrix, pos.x, pos.y, pos.z);
        VeilRenderSystem.renderer().getFramebufferManager().clear();
        VeilLevelRenderStages.begin((LevelRenderer) (Object) this, deltaTracker, camera, frustumMatrix, projectionMatrix, this.ticks);
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/renderer/culling/Frustum;"))
    private Frustum veil$captureFrustum(Frustum frustum) {
        VeilRenderSystem.setCullingFrustum(frustum);
        VeilLevelRenderStages.setFrustum(frustum);
        return frustum;
    }

    // ---- Main pass --------------------------------------------------------------------------------------------------

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal = 0))
    private void veil$afterSky(CallbackInfo ci) {
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_SKY);
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void veil$afterOpaque(CallbackInfo ci) {
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS);
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS);
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS);
    }

    @ModifyExpressionValue(method = "method_62214", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/vertex/PoseStack;"))
    private PoseStack veil$capturePoseStack(PoseStack poseStack) {
        VeilLevelRenderStages.setPoseStack(poseStack);
        return poseStack;
    }

    @WrapOperation(method = "method_62214",
            slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=submitEntities")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
    private void veil$afterEntities(OutlineBufferSource instance, Operation<Void> original) {
        original.call(instance);
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_ENTITIES);
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES);
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=string"))
    private void veil$afterTranslucent(CallbackInfo ci) {
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS);
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private void veil$afterTripwire(CallbackInfo ci) {
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS);
    }

    // ---- Particles / weather ----------------------------------------------------------------------------------------

    @Inject(method = "method_62213", at = @At("TAIL"))
    private void veil$afterParticles(CallbackInfo ci) {
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_PARTICLES);
    }

    @Inject(method = "method_62216", at = @At("TAIL"))
    private void veil$afterWeather(CallbackInfo ci) {
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_WEATHER);
    }

    // ---- End --------------------------------------------------------------------------------------------------------

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;execute(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder$Inspector;)V", shift = At.Shift.AFTER))
    private void veil$afterLevel(CallbackInfo ci) {
        VeilLevelRenderStages.fire(VeilRenderLevelStageEvent.Stage.AFTER_LEVEL);
        VeilLevelRenderStages.end();
    }
}
