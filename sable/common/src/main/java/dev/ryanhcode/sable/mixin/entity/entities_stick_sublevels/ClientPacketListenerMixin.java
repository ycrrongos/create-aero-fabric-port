package dev.ryanhcode.sable.mixin.entity.entities_stick_sublevels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.EntityStickExtension;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.packet_mixin.PacketActuallyInSubLevelExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow
    private ClientLevel level;

    @WrapOperation(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;moveOrInterpolateTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void sable$handleMoveEntityPosRot(final Entity instance, final Vec3 pos, final float yRot, final float xRot, final Operation<Void> original,
                                              @Local(argsOnly = true) final ClientboundMoveEntityPacket packet) {
        this.sable$lerp(instance, pos, Optional.of(yRot), Optional.of(xRot), false, sable$actuallyInSubLevel(packet));
    }

    @WrapOperation(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;moveOrInterpolateTo(Lnet/minecraft/world/phys/Vec3;)V"))
    private void sable$handleMoveEntityPos(final Entity instance, final Vec3 pos, final Operation<Void> original,
                                           @Local(argsOnly = true) final ClientboundMoveEntityPacket packet) {
        this.sable$lerp(instance, pos, Optional.empty(), Optional.empty(), false, sable$actuallyInSubLevel(packet));
    }

    @WrapOperation(method = "handleEntityPositionSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;moveOrInterpolateTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void sable$handlePositionSyncLerp(final Entity instance, final Vec3 pos, final float yRot, final float xRot, final Operation<Void> original,
                                              @Local(argsOnly = true) final ClientboundEntityPositionSyncPacket packet) {
        this.sable$lerp(instance, pos, Optional.of(yRot), Optional.of(xRot), false, sable$actuallyInSubLevel(packet));
    }

    @WrapOperation(method = "handleEntityPositionSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;snapTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void sable$handlePositionSyncSnap(final Entity instance, final Vec3 pos, final float yRot, final float xRot, final Operation<Void> original,
                                              @Local(argsOnly = true) final ClientboundEntityPositionSyncPacket packet) {
        final boolean actuallyInSubLevel = sable$actuallyInSubLevel(packet);
        if (actuallyInSubLevel || Sable.HELPER.getContaining(this.level, pos) != null) {
            this.sable$lerp(instance, pos, Optional.of(yRot), Optional.of(xRot), true, actuallyInSubLevel);
        } else {
            original.call(instance, pos, yRot, xRot);
            ((EntityStickExtension) instance).sable$setPlotPosition(null);
        }
    }

    @WrapOperation(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z"))
    private boolean sable$handleTeleportEntity(final PositionMoveRotation change, final Set<Relative> relatives, final Entity entity, final boolean lerp, final Operation<Boolean> original,
                                               @Local(argsOnly = true) final ClientboundTeleportEntityPacket packet) {
        final PositionMoveRotation absolute = PositionMoveRotation.calculateAbsolute(PositionMoveRotation.of(entity), change, relatives);
        final boolean actuallyInSubLevel = sable$actuallyInSubLevel(packet);
        if (actuallyInSubLevel || Sable.HELPER.getContaining(this.level, absolute.position()) != null) {
            entity.setDeltaMovement(absolute.deltaMovement());
            this.sable$lerp(entity, absolute.position(), Optional.of(absolute.yRot()), Optional.of(absolute.xRot()), !lerp, actuallyInSubLevel);
            return lerp;
        }

        final boolean result = original.call(change, relatives, entity, lerp);
        ((EntityStickExtension) entity).sable$setPlotPosition(null);
        return result;
    }

    @Unique
    private static boolean sable$actuallyInSubLevel(final Object packet) {
        return packet instanceof final PacketActuallyInSubLevelExtension extension && extension.sable$isActuallyInSubLevel();
    }

    @Unique
    private static void sable$moveOrSnap(final Entity entity, final Vec3 pos, final Optional<Float> yRot, final Optional<Float> xRot, final boolean snap) {
        if (snap) {
            entity.snapTo(pos, yRot.orElse(entity.getYRot()), xRot.orElse(entity.getXRot()));
        } else {
            entity.moveOrInterpolateTo(Optional.of(pos), yRot, xRot);
        }
    }

    @Unique
    private void sable$lerp(final Entity entity,
                            Vec3 pos,
                            final Optional<Float> yRot,
                            final Optional<Float> xRot,
                            final boolean snap,
                            final boolean actuallyInSubLevel) {
        final EntityStickExtension extension = (EntityStickExtension) entity;

        final SubLevelContainer container = SubLevelContainer.getContainer(this.level);
        final SubLevel subLevel = Sable.HELPER.getContaining(this.level, pos);
        final Vec3 plotPosition = extension.sable$getPlotPosition();

        if (!actuallyInSubLevel && subLevel == null && container.inBounds(BlockPos.containing(pos))) {
            return;
        }

        if (subLevel != null && !actuallyInSubLevel) {
            if (!(entity instanceof LivingEntity)) {
                pos = subLevel.logicalPose().transformPosition(pos);
                sable$moveOrSnap(entity, pos, yRot, xRot, snap);
                return;
            }

            if (plotPosition == null) {
                // just jumped on a sub-level
                extension.sable$setPlotPosition(subLevel.logicalPose().transformPositionInverse(entity.position()));
            } else {
                final SubLevel existingSubLevel = Sable.HELPER.getContaining(this.level, plotPosition);
                if (existingSubLevel != null && subLevel != existingSubLevel) {
                    final Vec3 globalPlotPos = existingSubLevel.logicalPose().transformPosition(plotPosition);
                    extension.sable$setPlotPosition(subLevel.logicalPose().transformPositionInverse(globalPlotPos));
                }
            }

            // Only the rotation of the vanilla interpolation is used, the position follows the plot position
            entity.moveOrInterpolateTo(Optional.empty(), yRot, xRot);

            // This does a custom position lerp
            if (snap) {
                extension.sable$setPlotPosition(pos);
            } else {
                extension.sable$plotLerpTo(pos, sable$interpolationSteps(entity));
            }
        } else {
            final SubLevel existingSubLevel = Sable.HELPER.getContaining(this.level, entity.position());

            if (subLevel != null && actuallyInSubLevel && existingSubLevel != subLevel) {
                entity.setPos(subLevel.logicalPose().transformPositionInverse(entity.position()));
            } else if (existingSubLevel != null && subLevel == null) {
                entity.setPos(existingSubLevel.logicalPose().transformPosition(entity.position()));
            }

            sable$moveOrSnap(entity, pos, yRot, xRot, snap);
            extension.sable$setPlotPosition(null);
        }
    }

    @Unique
    private static int sable$interpolationSteps(final Entity entity) {
        return entity.getInterpolation() instanceof final dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.InterpolationHandlerExtension handler
                ? handler.sable$getInterpolationSteps()
                : net.minecraft.world.entity.InterpolationHandler.DEFAULT_INTERPOLATION_STEPS;
    }
}
