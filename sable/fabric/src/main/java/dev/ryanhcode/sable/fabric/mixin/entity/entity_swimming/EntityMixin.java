package dev.ryanhcode.sable.fabric.mixin.entity.entity_swimming;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.math.LevelReusedVectors;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.LevelExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Makes entities swim in, get pushed by and see the fluids inside of sub-levels.
 */
@Mixin(value = Entity.class, priority = 500)
public abstract class EntityMixin {

    @Shadow
    private Level level;

    @Shadow
    private Vec3 position;

    @Shadow
    @Final
    private Set<TagKey<Fluid>> fluidOnEyes;

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract boolean isPushedByFluid();

    @Shadow
    public abstract BlockPos blockPosition();

    @Shadow
    public abstract Vec3 getEyePosition();

    @Shadow
    public abstract boolean touchingUnloadedChunk();

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(Vec3 deltaMovement);

    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    /**
     * @author RyanH
     * @reason Take into account the fluids inside of sub-levels.
     */
    @Overwrite
    public boolean updateFluidHeightAndDoFluidPushing(final TagKey<Fluid> fluidTag, final double motionScale) {
        if (this.touchingUnloadedChunk()) {
            return false;
        }

        final AABB aabb = this.getBoundingBox().deflate(0.001);
        final int i = Mth.floor(aabb.minX);
        final int j = Mth.ceil(aabb.maxX);
        final int k = Mth.floor(aabb.minY);
        final int l = Mth.ceil(aabb.maxY);
        final int m = Mth.floor(aabb.minZ);
        final int n = Mth.ceil(aabb.maxZ);
        double height = 0.0;
        final boolean pushedByFluid = this.isPushedByFluid();
        boolean inFluid = false;
        Vec3 flowVector = Vec3.ZERO;
        int flowBlockCount = 0;
        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int x = i; x < j; x++) {
            for (int y = k; y < l; y++) {
                for (int z = m; z < n; z++) {
                    blockPos.set(x, y, z);
                    final FluidState fluidState = this.level.getFluidState(blockPos);
                    if (fluidState.is(fluidTag)) {
                        final double fluidLevelY = y + fluidState.getHeight(this.level, blockPos);
                        if (fluidLevelY >= aabb.minY) {
                            inFluid = true;
                            height = Math.max(fluidLevelY - aabb.minY, height);
                            if (pushedByFluid) {
                                Vec3 flowVec = fluidState.getFlow(this.level, blockPos);
                                if (height < 0.4) {
                                    flowVec = flowVec.scale(height);
                                }

                                flowVector = flowVector.add(flowVec);
                                flowBlockCount++;
                            }
                        }
                    }
                }
            }
        }

        //#region sable stuff
        final ActiveSableCompanion helper = Sable.HELPER;
        final BoundingBox3d globalBounds = new BoundingBox3d(aabb);
        final BoundingBox3d localBounds = new BoundingBox3d();
        final Iterable<SubLevel> intersecting = helper.getAllIntersecting(this.level, globalBounds);

        final Vector3d playerCenter = new Vector3d();
        final Vector3d playerSize = new Vector3d();
        final Quaterniond playerOrientation = new Quaterniond();

        for (final SubLevel subLevel : intersecting) {
            final Pose3dc pose = subLevel.lastPose();
            globalBounds.transformInverse(pose, localBounds);

            final LevelReusedVectors jomlSink = ((LevelExtension) this.level).sable$getJOMLSink();
            final Quaterniond localPlayerBox = pose.orientation().conjugate(playerOrientation);

            final double yaw = SubLevelEntityCollision.getHitBoxYaw(pose);
            localPlayerBox.rotateY(yaw);

            final OrientedBoundingBox3d playerBox = new OrientedBoundingBox3d(pose.transformPositionInverse(globalBounds.center(playerCenter)), globalBounds.size(playerSize), localPlayerBox, jomlSink);
            final OrientedBoundingBox3d fluidBox = new OrientedBoundingBox3d(new Vector3d(), new Vector3d(1.0), JOMLConversion.QUAT_IDENTITY, jomlSink);

            final int minX = Mth.floor(localBounds.minX);
            final int maxX = Mth.ceil(localBounds.maxX);
            final int minY = Mth.floor(localBounds.minY);
            final int maxY = Mth.ceil(localBounds.maxY);
            final int minZ = Mth.floor(localBounds.minZ);
            final int maxZ = Mth.ceil(localBounds.maxZ);

            double minYVertex = Float.MAX_VALUE;
            boolean hasComputedMinYVertex = false;

            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        blockPos.set(x, y, z);
                        final FluidState fluidState = this.level.getFluidState(blockPos);

                        if (fluidState.is(fluidTag)) {
                            final double fluidLevelY = (float) y + fluidState.getHeight(this.level, blockPos);

                            if (!hasComputedMinYVertex) {
                                final Vector3d[] vertices = playerBox.vertices(jomlSink.a);

                                for (final Vector3d vertex : vertices) {
                                    minYVertex = Math.min(minYVertex, vertex.y);
                                }

                                hasComputedMinYVertex = true;
                            }

                            if (fluidLevelY >= minYVertex) {
                                fluidBox.getPosition().set(x + 0.5, y + 0.5, z + 0.5);

                                if (!(OrientedBoundingBox3d.sat(playerBox, fluidBox).lengthSquared() > 0.0))
                                    continue;

                                inFluid = true;
                                height = Math.max(fluidLevelY - minYVertex, height);

                                final Entity self = (Entity) (Object) this;
                                if (helper.getTrackingSubLevel(self) == null && helper.getContaining(self) != subLevel) {
                                    ((EntityMovementExtension) this).sable$setTrackingSubLevel(subLevel);
                                }

                                if (pushedByFluid) {
                                    Vec3 flowVec = fluidState.getFlow(this.level, blockPos);

                                    if (height < 0.4) {
                                        flowVec = flowVec.scale(height);
                                    }

                                    flowVec = pose.transformNormal(flowVec);

                                    flowVector = flowVector.add(flowVec);
                                    flowBlockCount++;
                                }
                            }
                        }
                    }
                }
            }
        }
        //#region sable end

        if (flowVector.length() > 0.0) {
            if (flowBlockCount > 0) {
                flowVector = flowVector.scale(1.0 / flowBlockCount);
            }

            if (!((Object) this instanceof Player)) {
                flowVector = flowVector.normalize();
            }

            final Vec3 deltaMovement = this.getDeltaMovement();
            flowVector = flowVector.scale(motionScale);
            if (Math.abs(deltaMovement.x) < 0.003 && Math.abs(deltaMovement.z) < 0.003 && flowVector.length() < 0.0045000000000000005) {
                flowVector = flowVector.normalize().scale(0.0045000000000000005);
            }

            this.setDeltaMovement(this.getDeltaMovement().add(flowVector));
        }

        this.fluidHeight.put(fluidTag, height);
        return inFluid;
    }

    /**
     * Entities can start swimming in the fluids of sub-levels
     */
    @WrapOperation(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean sable$canStartSwimmingInSubLevel(final FluidState instance, final TagKey<Fluid> tag, final Operation<Boolean> original) {
        if (original.call(instance, tag)) {
            return true;
        }

        final BlockPos globalBlockPos = this.blockPosition();
        final Iterable<SubLevel> intersecting = Sable.HELPER.getAllIntersecting(this.level, new BoundingBox3d(globalBlockPos).expand(0.5));

        for (final SubLevel subLevel : intersecting) {
            final Pose3dc pose = subLevel.lastPose();

            final BlockPos localBlockPos = BlockPos.containing(pose.transformPositionInverse(this.position));
            if (this.level.getFluidState(localBlockPos).is(tag)) {
                return true;
            }
        }

        return false;
    }

    @Inject(method = "updateFluidOnEyes", at = @At("TAIL"))
    private void sable$subLevelFluidOnEyes(final CallbackInfo ci) {
        if (!this.fluidOnEyes.isEmpty()) {
            return;
        }

        final Vec3 globalEyePos = this.getEyePosition();
        final Iterable<SubLevel> intersecting = Sable.HELPER.getAllIntersecting(this.level, new BoundingBox3d(BlockPos.containing(globalEyePos)).expand(0.5));

        for (final SubLevel subLevel : intersecting) {
            final Pose3dc pose = subLevel.lastPose();
            final Vec3 localEyePos = pose.transformPositionInverse(globalEyePos);
            final BlockPos blockPos = BlockPos.containing(localEyePos);

            final FluidState fluidState = this.level.getFluidState(blockPos);
            final double e = (float) blockPos.getY() + fluidState.getHeight(this.level, blockPos);

            if (e > localEyePos.y) {
                fluidState.getTags().forEach(this.fluidOnEyes::add);

                if (!this.fluidOnEyes.isEmpty()) {
                    return;
                }
            }
        }
    }
}
