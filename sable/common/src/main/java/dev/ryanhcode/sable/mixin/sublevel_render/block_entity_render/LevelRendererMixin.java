package dev.ryanhcode.sable.mixin.sublevel_render.block_entity_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinhelpers.sublevel_render.vanilla.VanillaSubLevelBlockEntityRenderer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.dispatcher.SubLevelRenderDispatcher;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Extracts and submits the block entities of sub-levels.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Unique
    private VanillaSubLevelBlockEntityRenderer sable$subLevelBlockEntityRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void sable$init(final CallbackInfo ci) {
        this.sable$subLevelBlockEntityRenderer = new VanillaSubLevelBlockEntityRenderer(this.blockEntityRenderDispatcher, this.destructionProgress);
    }

    /**
     * Globally rendered block entities of sub-levels are extracted with the rest of the sub-level block entities
     */
    @WrapOperation(method = "extractVisibleBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;", ordinal = 1))
    private BlockEntityRenderState sable$skipGlobalSubLevelBlockEntities(final BlockEntityRenderDispatcher instance, final BlockEntity blockEntity, final float partialTick,
                                                                         final @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, final Operation<BlockEntityRenderState> original) {
        if (Sable.HELPER.getContainingClient(blockEntity) != null) {
            return null;
        }
        return original.call(instance, blockEntity, partialTick, crumblingOverlay);
    }

    @Inject(method = "extractVisibleBlockEntities", at = @At("TAIL"))
    private void sable$extractSubLevelBlockEntities(final Camera camera, final float partialTick, final LevelRenderState renderState, final CallbackInfo ci) {
        final VanillaSubLevelBlockEntityRenderer renderer = this.sable$subLevelBlockEntityRenderer;
        renderer.clear();

        final ClientSubLevelContainer container = SubLevelContainer.getContainer(this.level);
        if (container == null) {
            return;
        }

        final Vec3 cameraPosition = camera.position();
        final SubLevelRenderDispatcher dispatcher = SubLevelRenderDispatcher.get();
        final List<ClientSubLevel> visibleSubLevels = new ArrayList<>();
        for (final ClientSubLevel subLevel : container.getAllSubLevels()) {
            if (dispatcher.isVisible(subLevel)) {
                visibleSubLevels.add(subLevel);
            }
        }
        dispatcher.renderBlockEntities(visibleSubLevels, renderer, cameraPosition.x, cameraPosition.y, cameraPosition.z, partialTick);

        final Matrix4f transformation = new Matrix4f();
        for (final BlockEntity blockEntity : this.level.getGloballyRenderedBlockEntities()) {
            if (blockEntity.isRemoved()) {
                continue;
            }

            final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(blockEntity);
            if (subLevel == null || !dispatcher.isVisible(subLevel)) {
                continue;
            }

            subLevel.getRenderData().getTransformation(cameraPosition.x, cameraPosition.y, cameraPosition.z, transformation);
            final Vec3 localCamera = subLevel.renderPose().transformPositionInverse(cameraPosition);
            renderer.renderSingleBE(blockEntity, subLevel, transformation, localCamera, partialTick);
        }
    }

    @Inject(method = "submitBlockEntities", at = @At("TAIL"))
    private void sable$submitSubLevelBlockEntities(final PoseStack poseStack, final LevelRenderState renderState, final SubmitNodeStorage nodeStorage, final CallbackInfo ci) {
        this.sable$subLevelBlockEntityRenderer.submit(poseStack, nodeStorage);
    }
}
