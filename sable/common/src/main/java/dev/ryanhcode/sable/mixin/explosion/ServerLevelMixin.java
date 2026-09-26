package dev.ryanhcode.sable.mixin.explosion;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    public abstract ServerLevel getLevel();

    /**
     * Projects explosions happening inside of a sub-level plot out into the world.
     * In 1.21.11 {@code ServerLevel#explode} returns {@code void} and takes the weighted block particles.
     */
    @WrapMethod(method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/util/random/WeightedList;Lnet/minecraft/core/Holder;)V")
    public void sable$preExplode(final Entity entity,
                                 final DamageSource damageSource,
                                 final ExplosionDamageCalculator explosionDamageCalculator,
                                 final double d,
                                 final double e,
                                 final double f,
                                 final float g,
                                 final boolean bl,
                                 final Level.ExplosionInteraction explosionInteraction,
                                 final ParticleOptions particleOptions,
                                 final ParticleOptions particleOptions2,
                                 final WeightedList<ExplosionParticleInfo> blockParticles,
                                 final Holder<SoundEvent> holder,
                                 final Operation<Void> original) {

        final Vector3d projectedPos = Sable.HELPER.projectOutOfSubLevel(this.getLevel(), new Vector3d(d, e, f));
        original.call(entity, damageSource, explosionDamageCalculator, projectedPos.x, projectedPos.y, projectedPos.z, g, bl, explosionInteraction, particleOptions, particleOptions2, blockParticles, holder);
    }
}
