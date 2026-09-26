package dev.ryanhcode.sable.mixin.explosion;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Makes explosion rays sub-level aware.
 * <p>
 * In 1.21.11 {@code Explosion} is an interface, and the block ray-marching that used to live in
 * {@code Explosion#explode} was moved to {@link ServerExplosion#calculateExplodedPositions()}.
 * Whenever a ray passes through an empty world block, the blocks of every intersecting sub-level at that
 * point are taken into account for the resistance, get exploded, and push the sub-level away.
 */
@Mixin(ServerExplosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Vec3 center;

    @Shadow
    @Final
    private ExplosionDamageCalculator damageCalculator;

    @Shadow @Final private @Nullable Entity source;

    @Inject(method = "calculateExplodedPositions", at = @At("HEAD"))
    private void sable$preExplode(final CallbackInfoReturnable<List<BlockPos>> cir, @Share("explodedSet") final LocalRef<Set<BlockPos>> explodedSet) {
        explodedSet.set(new ObjectOpenHashSet<>());
    }

    @Inject(method = "calculateExplodedPositions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"))
    private void sable$redirectBlockExplosionResistance(final CallbackInfoReturnable<List<BlockPos>> cir,
                                                        @Local final Set<BlockPos> set,
                                                        @Local(ordinal = 4) final double rayX,
                                                        @Local(ordinal = 5) final double rayY,
                                                        @Local(ordinal = 6) final double rayZ,
                                                        @Local final BlockPos worldBlockPos,
                                                        @Local final BlockState worldBlockState,
                                                        @Local(ordinal = 0) final LocalFloatRef fReference,
                                                        @Share("explodedSet") final LocalRef<Set<BlockPos>> explodedSet) {
        final ServerExplosion self = (ServerExplosion) (Object) this;

        if (!worldBlockState.isAir()) {
            return;
        }

        float f = fReference.get();

        final BoundingBox3d globalBounds = new BoundingBox3d(worldBlockPos);
        final Iterable<SubLevel> subLevels = Sable.HELPER.getAllIntersecting(this.level, globalBounds);
        final SubLevelContainer container = SubLevelContainer.getContainer(this.level);

        for (final SubLevel subLevel : subLevels) {
            final Pose3d pose = subLevel.logicalPose();
            final Vec3 localRayPosition = pose.transformPositionInverse(new Vec3(rayX, rayY, rayZ));
            final Vec3 localExplosionPosition = pose.transformPositionInverse(this.center);

            final BlockPos blockpos = BlockPos.containing(localRayPosition);
            final BlockState blockstate = this.level.getBlockState(blockpos);
            final FluidState fluidstate = this.level.getFluidState(blockpos);

            final boolean canExplodeBefore = f > 0.0;

            final Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(self, this.level, blockpos, blockstate, fluidstate);
            if (optional.isPresent()) {
                f -= (optional.get() + 0.3F) * 0.3F;
            }

            if (f > 0.0F && this.damageCalculator.shouldBlockExplode(self, this.level, blockpos, blockstate, f)) {
                set.add(blockpos);
            }

            final boolean wind = (this.source instanceof AbstractWindCharge || this.damageCalculator == AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR) && !blockstate.isAir();
            if (canExplodeBefore && (f < 0.0f || wind) && explodedSet.get().add(blockpos)) {
                explodedSet.get().add(blockpos);

                if (subLevel instanceof final ServerSubLevel serverSubLevel) {
                    final SubLevelPhysicsSystem physicsSystem = ((ServerSubLevelContainer) container).physicsSystem();
                    final RigidBodyHandle handle = physicsSystem.getPhysicsHandle(serverSubLevel);

                    final Vec3 pos = blockpos.getCenter();
                    final Vec3 force = pos.subtract(localExplosionPosition).normalize().scale(5.0);
                    handle.applyImpulseAtPoint(pos, force);
                }
            }
        }

        fReference.set(f);
    }
}
