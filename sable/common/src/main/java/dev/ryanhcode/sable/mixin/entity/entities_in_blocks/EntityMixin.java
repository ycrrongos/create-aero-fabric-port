package dev.ryanhcode.sable.mixin.entity.entities_in_blocks;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Makes entities receive the "inside block" effects (cobwebs, portals, fluids, pressure plates etc.) of sub-level blocks they intersect
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    /**
     * Sub-level block effects are collected in their own step, after all vanilla movement steps
     */
    @Unique
    private static final int SABLE$SUB_LEVEL_STEP = Integer.MAX_VALUE;

    @Shadow
    public abstract boolean isAlive();

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    private Level level;

    @Shadow
    protected abstract void onInsideBlock(BlockState blockState);

    @Shadow
    protected abstract boolean isAffectedByBlocks();

    @Shadow
    public abstract void fillCrashReportCategory(CrashReportCategory category);

    @Inject(method = "checkInsideBlocks(Ljava/util/List;Lnet/minecraft/world/entity/InsideBlockEffectApplier$StepBasedCollector;)V", at = @At("TAIL"))
    protected void sable$checkInsideSubLevelBlocks(final List<?> movements, final InsideBlockEffectApplier.StepBasedCollector collector, final CallbackInfo ci) {
        if (!this.isAffectedByBlocks()) {
            return;
        }

        final AABB bounds = this.getBoundingBox();

        final BoundingBox3d localBounds = new BoundingBox3d(bounds);
        for (final SubLevel intersecting : Sable.HELPER.getAllIntersecting(this.level, new BoundingBox3d(bounds))) {
            localBounds.set(bounds);
            localBounds.transformInverse(intersecting.logicalPose(), localBounds);
            final AABB localAABB = new AABB(localBounds.minX, localBounds.minY, localBounds.minZ, localBounds.maxX, localBounds.maxY, localBounds.maxZ).deflate(1.0E-5F);
            final BlockPos minPos = BlockPos.containing(localAABB.minX, localAABB.minY, localAABB.minZ);
            final BlockPos maxPos = BlockPos.containing(localAABB.maxX, localAABB.maxY, localAABB.maxZ);

            if (!this.level.hasChunksAt(minPos, maxPos)) {
                continue;
            }

            final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

            for (int i = minPos.getX(); i <= maxPos.getX(); i++) {
                for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
                    for (int k = minPos.getZ(); k <= maxPos.getZ(); k++) {
                        if (!this.isAlive()) {
                            return;
                        }

                        mutableBlockPos.set(i, j, k);
                        final BlockState blockState = this.level.getBlockState(mutableBlockPos);
                        if (blockState.isAir()) {
                            continue;
                        }

                        this.sable$entityInside(blockState, mutableBlockPos.immutable(), localAABB, collector);
                    }
                }
            }
        }
    }

    @Unique
    private void sable$entityInside(final BlockState blockState, final BlockPos pos, final AABB localBounds, final InsideBlockEffectApplier.StepBasedCollector collector) {
        final Entity self = (Entity) (Object) this;

        final VoxelShape shape = blockState.getEntityInsideCollisionShape(this.level, pos, self);
        final boolean insideBlock = shape == Shapes.block() || sable$intersectsAny(localBounds, shape.move(pos).toAabbs());

        final FluidState fluidState = blockState.getFluidState();
        final AABB fluidBounds = fluidState.isEmpty() ? null : fluidState.getAABB(this.level, pos);
        final boolean insideFluid = fluidBounds != null && localBounds.intersects(fluidBounds);

        if (insideBlock) {
            try {
                collector.advanceStep(SABLE$SUB_LEVEL_STEP);
                blockState.entityInside(this.level, pos, self, collector, localBounds.intersects(pos));
                this.onInsideBlock(blockState);
            } catch (final Throwable throwable) {
                final CrashReport crashReport = CrashReport.forThrowable(throwable, "Colliding entity with sub-level block");
                final CrashReportCategory blockCategory = crashReport.addCategory("Block being collided with");
                CrashReportCategory.populateBlockDetails(blockCategory, this.level, pos, blockState);
                final CrashReportCategory entityCategory = crashReport.addCategory("Entity being checked for collision");
                this.fillCrashReportCategory(entityCategory);
                throw new ReportedException(crashReport);
            }
        }

        if (insideFluid) {
            collector.advanceStep(SABLE$SUB_LEVEL_STEP);
            fluidState.entityInside(this.level, pos, self, collector);
        }
    }

    @Unique
    private static boolean sable$intersectsAny(final AABB bounds, final List<AABB> boxes) {
        for (final AABB box : boxes) {
            if (bounds.intersects(box)) {
                return true;
            }
        }
        return false;
    }
}
