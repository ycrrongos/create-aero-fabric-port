package dev.ryanhcode.sable.mixin.sublevel_render.impl.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuSampler;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.SableClient;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.plot.SubLevelContainerHolder;
import dev.ryanhcode.sable.render.sky_light_shadow.SableSkyLightShadows;
import dev.ryanhcode.sable.render.terrain.SableTerrainShader;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderContext;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.SubLevelSectionDraws;
import dev.ryanhcode.sable.sublevel.render.dispatcher.SubLevelRenderDispatcher;
import dev.ryanhcode.sable.sublevel.render.dispatcher.VanillaSubLevelRenderDispatcher;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Compiles, culls and draws sub-levels alongside the vanilla level sections.
 * <p>
 * Sub-level sections are drawn right after each vanilla chunk layer group, with the same terrain pipelines and output
 * targets, so they take part in the regular opaque, translucent and tripwire passes.
 */
@Mixin(value = LevelRenderer.class, priority = 1002) // Higher priority to go after Flywheel
public abstract class LevelRendererMixin {

    @Shadow
    private @Nullable ClientLevel level;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private @Nullable Frustum capturedFrustum;

    @Shadow
    private @Nullable GpuSampler chunkLayerSampler;

    @Unique
    private @Nullable SubLevelSectionDraws sable$draws;

    @Unique
    private Iterable<ClientSubLevel> sable$getSubLevels() {
        return ((ClientSubLevelContainer) ((SubLevelContainerHolder) this.level).sable$getPlotContainer()).getAllSubLevels();
    }

    @Inject(method = "compileSections", at = @At("TAIL"))
    private void sable$compileSections(final Camera camera, final CallbackInfo ci) {
        final RenderRegionCache renderRegionCache = new RenderRegionCache();
        final PrioritizeChunkUpdates chunkUpdates = this.minecraft.options.prioritizeChunkUpdates().get();

        for (final ClientSubLevel sublevel : this.sable$getSubLevels()) {
            sublevel.getRenderData().compileSections(chunkUpdates, renderRegionCache, camera);
        }
    }

    @Inject(method = "cullTerrain", at = @At("TAIL"))
    private void sable$cull(final Camera camera, final Frustum frustum, final boolean isSpectator, final CallbackInfo ci) {
        if (this.capturedFrustum != null) {
            return;
        }

        final SubLevelRenderDispatcher dispatcher = SubLevelRenderDispatcher.get();
        dispatcher.preRenderChunks(camera);

        final Vec3 cameraPosition = camera.position();
        dispatcher.updateCulling(this.sable$getSubLevels(), cameraPosition.x, cameraPosition.y, cameraPosition.z, frustum, isSpectator);
    }

    @Inject(method = "isSectionCompiledAndVisible", at = @At("HEAD"), cancellable = true)
    private void sable$isSectionCompiled(final BlockPos blockPos, final CallbackInfoReturnable<Boolean> cir) {
        final ClientSubLevelContainer container = SubLevelContainer.getContainer(this.level);

        if (container == null) {
            return;
        }

        if (container.inBounds(blockPos)) {
            final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(this.level, blockPos);

            if (subLevel == null) {
                cir.setReturnValue(false);
            } else {
                final SubLevelRenderData renderData = subLevel.getRenderData();
                final SectionPos sectionPos = SectionPos.of(blockPos);
                cir.setReturnValue(renderData.isSectionCompiled(sectionPos.x(), sectionPos.y(), sectionPos.z()));
            }
        }
    }

    @ModifyExpressionValue(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareChunkRenders(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;"))
    private ChunkSectionsToRender sable$prepareSubLevels(final ChunkSectionsToRender original,
                                                         @Local(argsOnly = true) final LevelRenderState renderState,
                                                         @Local(argsOnly = true) final Matrix4f frustumMatrix) {
        final Vec3 camera = renderState.cameraRenderState.pos;
        final float partialTicks = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        // World terrain uniforms, the sub-level passes restore these after drawing
        SableTerrainShader.updateFrame(this.level);
        for (final var pipeline : SableTerrainShader.PIPELINES) {
            SableTerrainShader.setupWorld(pipeline, SableSkyLightShadows.isEnabled());
            VanillaSubLevelRenderDispatcher.setupDynamicEffects(pipeline, false);
        }
        SableSkyLightShadows.renderShadowMap(this.level, camera, partialTicks);
        SableSkyLightShadows.bindShadowMap(camera);
        SableClient.WATER_OCCLUSION_RENDERER.preRenderTranslucent(frustumMatrix, camera);

        final SubLevelRenderContext context = new SubLevelRenderContext(frustumMatrix, camera.x, camera.y, camera.z, partialTicks,
                this.chunkLayerSampler, true, true);
        this.sable$draws = SubLevelRenderDispatcher.get().prepareSections(this.sable$getSubLevels(), context);
        return original;
    }

    @WrapOperation(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V"))
    private void sable$renderSubLevelGroup(final ChunkSectionsToRender instance, final ChunkSectionLayerGroup group, final GpuSampler sampler, final Operation<Void> original) {
        final boolean translucent = group == ChunkSectionLayerGroup.TRANSLUCENT;
        if (translucent) {
            SableClient.WATER_OCCLUSION_RENDERER.setupTranslucentShader();
        }

        original.call(instance, group, sampler);

        if (translucent) {
            SableClient.WATER_OCCLUSION_RENDERER.clearTranslucentShader();
        }

        final SubLevelSectionDraws draws = this.sable$draws;
        if (draws == null) {
            return;
        }

        final RenderTarget target = group.outputTarget();
        final SubLevelRenderDispatcher dispatcher = SubLevelRenderDispatcher.get();
        for (final ChunkSectionLayer layer : group.layers()) {
            dispatcher.renderSectionLayer(draws, layer, target.getColorTextureView(), target.getDepthTextureView());
        }
    }

    @Inject(method = "method_62214", at = @At("TAIL"))
    private void sable$finishSubLevels(final CallbackInfo ci) {
        this.sable$draws = null;
    }
}
