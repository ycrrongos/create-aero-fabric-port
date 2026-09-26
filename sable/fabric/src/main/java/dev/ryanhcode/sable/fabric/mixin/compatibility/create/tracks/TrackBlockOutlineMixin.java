package dev.ryanhcode.sable.fabric.mixin.compatibility.create.tracks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Note: upstream also redirected the {@code PoseStack#translate} in {@code drawCustomBlockSelection} to move normal
 * track block outlines onto their sub-level. On 1.21.11, Create Fly calls {@code drawCustomBlockSelection} from inside
 * {@code LevelRenderer#renderBlockOutline}, which Sable's {@code block_outline_render.LevelRendererMixin} already wraps
 * with the pose stack moved into the local space of the sub-level and the camera render state moved to the plot-local
 * camera position, so the outline already renders on the sub-level without an extra transform.
 */
@Mixin(TrackBlockOutline.class)
public class TrackBlockOutlineMixin {

    /**
     * Translating the render of curve sections, rotation is not needed
     * */
    @WrapOperation(method = "drawCurveSelection", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/flywheel/lib/transform/PoseTransformStack;translate(DDD)Lcom/zurrtum/create/client/flywheel/lib/transform/Translate;", ordinal = 0))
    private static Translate<?> sable$translateCurveFactoringSubLevels(final PoseTransformStack ms,
                                                                       final double x,
                                                                       final double y,
                                                                       final double z,
                                                                       final Operation<Translate<?>> original,
                                                                       @Local final TrackBlockOutline.BezierPointSelection result,
                                                                       @Local(argsOnly = true) final Vec3 camera) {
        final Level level = Minecraft.getInstance().level;

        if (level == null) {
            return original.call(ms, x, y, z);
        }

        final Vec3 bezierPos = result.vec();
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(level, bezierPos);

        if (subLevel == null) {
            return original.call(ms, x, y, z);
        }

        // The plot-space point the original translation targets (curve anchor plus the vertical selection offset)
        final Vec3 plotPos = new Vec3(x + camera.x, y + camera.y, z + camera.z);

        Vec3 worldPos = subLevel.renderPose().transformPosition(plotPos);
        worldPos = worldPos.subtract(camera);
        return ms
                .translate(worldPos.x, worldPos.y, worldPos.z)
                .rotate(new Quaternionf(subLevel.renderPose().orientation()));
    }

    /**
     * Provides the subLevel to the other 2 redirects so that the curve can translate its bounds and position to world space
     */
    @Inject(method = "pickCurves", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/track/BezierConnection;isPrimary()Z"))
    private static void sable$findBlockEntitySubLevel(final Minecraft mc,
                                                      final CallbackInfo ci,
                                                      @Share("currentBlockEntitySubLevel") final LocalRef<ClientSubLevel> subLevel,
                                                      @Local final TrackBlockEntity be) {
        subLevel.set((ClientSubLevel) Sable.HELPER.getContaining(be));
    }

    @Redirect(method = "pickCurves", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/track/BezierConnection;getBounds()Lnet/minecraft/world/phys/AABB;"))
    private static AABB sable$getWorldSpaceBounds(final BezierConnection instance,
                                                  @Share("currentBlockEntitySubLevel") final LocalRef<ClientSubLevel> subLevel) {
        if (subLevel.get() == null) {
            return instance.getBounds();
        }

        final float partialTicks = AnimationTickHolder.getPartialTicks(Minecraft.getInstance().level);
        final BoundingBox3d localBounds = new BoundingBox3d(instance.getBounds()).transform(subLevel.get().renderPose(partialTicks));
        return localBounds.toMojang();
    }

    @Redirect(method = "pickCurves", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 sable$getEyePosition(final LocalPlayer entity, final float partialTicks) {
        return Sable.HELPER.getEyePositionInterpolated(entity, partialTicks);
    }

    @Redirect(method = "pickCurves", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private static double sable$distanceToHitSquared(final Vec3 vecA, final Vec3 vecB) {
        return Sable.HELPER.distanceSquaredWithSubLevels(Minecraft.getInstance().level, vecA, vecB);
    }

    @Redirect(method = "pickCurves", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", ordinal = 1))
    private static Vec3 sable$getLocalOrigin(final Vec3 origin,
                                             final Vec3 anchor,
                                             @Share("currentBlockEntitySubLevel") final LocalRef<ClientSubLevel> subLevel) {
        if (subLevel.get() == null) {
            return origin.subtract(anchor);
        }

        final float partialTicks = AnimationTickHolder.getPartialTicks(Minecraft.getInstance().level);
        final Vec3 localOrigin = subLevel.get().renderPose(partialTicks).transformPositionInverse(origin);
        return localOrigin.subtract(anchor);
    }

    @Redirect(method = "pickCurves", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", ordinal = 2))
    private static Vec3 sable$getLocalTarget(final Vec3 target,
                                             final Vec3 origin,
                                             @Share("currentBlockEntitySubLevel") final LocalRef<ClientSubLevel> subLevel) {
        if (subLevel.get() == null) {
            return target.subtract(origin);
        }

        final float partialTicks = AnimationTickHolder.getPartialTicks(Minecraft.getInstance().level);
        final Vec3 localTarget = subLevel.get().renderPose(partialTicks).transformPositionInverse(target);
        final Vec3 localOrigin = subLevel.get().renderPose(partialTicks).transformPositionInverse(origin);
        return localTarget.subtract(localOrigin);
    }

}
