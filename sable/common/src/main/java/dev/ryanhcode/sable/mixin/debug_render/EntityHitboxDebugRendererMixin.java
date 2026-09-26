package dev.ryanhcode.sable.mixin.debug_render;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinhelpers.debug.OrientedBoxGizmo;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.EntityStickExtension;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.LivingEntityStickExtension;
import dev.ryanhcode.sable.network.client.ClientSableInterpolationState;
import dev.ryanhcode.sable.network.client.SubLevelSnapshotInterpolator;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shows sub-level bounds and the sub-level related state of entities while hitboxes are shown (F3+B).
 */
@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {

    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(method = "emitGizmos", at = @At("TAIL"))
    private void sable$emitSubLevelBounds(final double camX, final double camY, final double camZ, final DebugValueAccess debugValueAccess, final Frustum frustum, final float partialTick, final CallbackInfo ci) {
        if (this.minecraft.level == null || this.minecraft.showOnlyReducedInfo()) {
            return;
        }

        final SubLevelContainer container = SubLevelContainer.getContainer(this.minecraft.level);
        if (container == null) {
            return;
        }

        for (final SubLevel subLevel : container.getAllSubLevels()) {
            final BoundingBox3dc bounds = subLevel.boundingBox();
            Gizmos.cuboid(new AABB(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ()),
                    GizmoStyle.stroke(ARGB.colorFromFloat(0.7F, 0.5F, 0.5F, 0.5F)));

            final Pose3dc renderPose = ((ClientSubLevel) subLevel).renderPose();
            final Vector3dc localCenter = renderPose.rotationPoint();
            final BoundingBox3ic plotBounds = subLevel.getPlot().getBoundingBox();

            // The center of mass
            OrientedBoxGizmo.inPlot(new AABB(localCenter.x(), localCenter.y(), localCenter.z(), localCenter.x(), localCenter.y(), localCenter.z()).inflate(2.0 / 16.0),
                    renderPose, GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 0.7F, 0.7F, 0.5F)));

            // The plot
            OrientedBoxGizmo.inPlot(new AABB(plotBounds.minX(), plotBounds.minY(), plotBounds.minZ(), plotBounds.maxX() + 1.0, plotBounds.maxY() + 1.0, plotBounds.maxZ() + 1.0),
                    renderPose, GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 0.9F, 0.5F, 0.5F)));

            if (ClientSableInterpolationState.RENDER_INTERPOLATION_BOUNDS) {
                final Vector3d boundSize = bounds.size(new Vector3d());
                final SubLevelSnapshotInterpolator interpolator = ((ClientSubLevel) subLevel).getInterpolator();
                for (final SubLevelSnapshotInterpolator.Snapshot buffer : interpolator.buffer) {
                    final Pose3dc pose = buffer.pose();

                    Gizmos.cuboid(new AABB(
                            pose.position().x() - boundSize.x() / 2.0,
                            pose.position().y() - boundSize.y() / 2.0,
                            pose.position().z() - boundSize.z() / 2.0,
                            pose.position().x() + boundSize.x() / 2.0,
                            pose.position().y() + boundSize.y() / 2.0,
                            pose.position().z() + boundSize.z() / 2.0
                    ), GizmoStyle.stroke(ARGB.colorFromFloat(0.5F, 0.0F, 1.0F, 1.0F)));
                }
            }
        }
    }

    @Inject(method = "showHitboxes", at = @At("TAIL"))
    private void sable$showSubLevelHitboxes(final Entity entity, final float partialTicks, final boolean isServerEntity, final CallbackInfo ci) {
        // collision hitbox
        final SubLevel tracking = Sable.HELPER.getTrackingSubLevel(entity);
        final Vec3 renderPosition = entity.getPosition(partialTicks);

        if (tracking instanceof final ClientSubLevel clientSubLevel) {
            Quaterniondc customOrientation = EntitySubLevelUtil.getCustomEntityOrientation(entity, partialTicks);
            if (customOrientation == null) {
                customOrientation = JOMLConversion.QUAT_IDENTITY;
            }

            final double yaw = SubLevelEntityCollision.getHitBoxYaw(clientSubLevel.renderPose());
            final Quaterniond orientation = new Quaterniond(customOrientation).rotateY(yaw);

            final AABB bounds = entity.getBoundingBox().move(renderPosition.subtract(entity.position()));
            final Vec3 pivot = renderPosition.add(0.0, entity.getEyeHeight(), 0.0);
            OrientedBoxGizmo.rotated(bounds, pivot, orientation, GizmoStyle.stroke(ARGB.colorFromFloat(0.4F, 1.0F, 1.0F, 0.0F)));
        }

        final EntityStickExtension duck = (EntityStickExtension) entity;
        final Vec3 plotPosition = duck.sable$getPlotPosition();
        if (plotPosition != null) {
            final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(entity.level(), plotPosition);
            if (subLevel != null) {
                final Vec3 projectedPos = subLevel.renderPose().transformPosition(plotPosition);
                Gizmos.cuboid(entity.getType().getSpawnAABB(projectedPos.x, projectedPos.y, projectedPos.z), GizmoStyle.stroke(ARGB.colorFromFloat(0.2F, 0.0F, 1.0F, 0.0F)));

                if (entity instanceof final LivingEntityStickExtension livingDuck) {
                    final Vec3 serverProjectedPos = subLevel.renderPose().transformPosition(livingDuck.sable$getLerpTarget());
                    Gizmos.cuboid(entity.getType().getSpawnAABB(serverProjectedPos.x, serverProjectedPos.y, serverProjectedPos.z), GizmoStyle.stroke(ARGB.colorFromFloat(0.2F, 1.0F, 0.0F, 1.0F)));
                }
            }
        }
    }
}
