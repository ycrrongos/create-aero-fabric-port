package dev.ryanhcode.sable.fabric.mixin.compatibility.create.tracks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.content.trains.track.TrackTargetingClient;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create Fly renders the track targeting overlay through a {@link TrackBlockRenderState} whose transform starts with a
 * camera-relative block offset instead of translating the pose stack. For tracks on sub-levels, the overlay state is
 * built block-local (zero offset), and the pose stack is moved to the block on its sub-level before rendering it.
 */
@Mixin(TrackTargetingClient.class)
public class TrackTargetingClientMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/content/trains/track/TrackBlockRenderer;getRenderState(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$AxisDirection;Lcom/zurrtum/create/infrastructure/component/BezierTrackPointLocation;Lcom/zurrtum/create/content/trains/track/TrackTargetingBehaviour$RenderedTrackOverlayType;F)Lcom/zurrtum/create/client/content/trains/track/TrackBlockRenderState;"))
    private static TrackBlockRenderState sable$useLocalOffset(final TrackBlockRenderer renderer,
                                                              final Level level,
                                                              final Vec3 offset,
                                                              final BlockState trackState,
                                                              final BlockPos pos,
                                                              final Direction.AxisDirection direction,
                                                              final BezierTrackPointLocation bezier,
                                                              final TrackTargetingBehaviour.RenderedTrackOverlayType type,
                                                              final float scale,
                                                              final Operation<TrackBlockRenderState> original) {
        if (Sable.HELPER.getContaining(level, pos) instanceof ClientSubLevel) {
            // The camera-relative translation and sub-level rotation are applied to the pose stack in sable$manipulateMatrixStack
            return original.call(renderer, level, Vec3.ZERO, trackState, pos, direction, bezier, type, scale);
        }

        return original.call(renderer, level, offset, trackState, pos, direction, bezier, type, scale);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/content/trains/track/TrackBlockRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private static void sable$manipulateMatrixStack(final TrackBlockRenderState instance,
                                                    final PoseStack ms,
                                                    final MultiBufferSource buffer,
                                                    final Operation<Void> original,
                                                    @Local(argsOnly = true) final Minecraft minecraft,
                                                    @Local final BlockPos pos,
                                                    @Local(argsOnly = true) final Vec3 camera) {
        final ClientLevel level = minecraft.level;
        final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);

        if (subLevel instanceof final ClientSubLevel clientSubLevel) {
            final Pose3dc renderPose = clientSubLevel.renderPose();
            final Vec3 renderPos = renderPose.transformPosition(Vec3.atLowerCornerOf(pos));
            final Quaternionf renderOrientation = new Quaternionf(renderPose.orientation());

            ms.pushPose();
            ms.translate(renderPos.x() - camera.x(), renderPos.y() - camera.y(), renderPos.z() - camera.z());
            ms.mulPose(renderOrientation);
            original.call(instance, ms, buffer);
            ms.popPose();
            return;
        }

        original.call(instance, ms, buffer);
    }

}
