package dev.ryanhcode.sable.mixin.entity.entity_sublevel_collision;

import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Minimal 1.21.11 stub: full {@code EntityMixin} injects are stripped, but Sable still casts
 * entities to {@link EntityMovementExtension}. Provide no-op storage so ticks/join don't CCE.
 */
@Mixin(Entity.class)
public abstract class EntityMovementExtensionStubMixin implements EntityMovementExtension {

    @Unique
    private SubLevelEntityCollision.CollisionInfo sable$stubCollisionInfo;

    @Unique
    private SubLevel sable$stubTrackingSubLevel;

    @Unique
    private UUID sable$stubLastTrackingId;

    @Unique
    private BlockPos sable$stubInBlockStatePos = BlockPos.ZERO;

    @Override
    public SubLevelEntityCollision.CollisionInfo sable$getCollisionInfo() {
        return this.sable$stubCollisionInfo;
    }

    @Override
    public SubLevel sable$getTrackingSubLevel() {
        return this.sable$stubTrackingSubLevel;
    }

    @Override
    public UUID sable$getLastTrackingSubLevelID() {
        return this.sable$stubLastTrackingId;
    }

    @Override
    public void sable$setPosField(final Vec3 vec3) {
        // no-op: full movement rewrite not ported yet
    }

    @Override
    public void sable$setTrackingSubLevel(final SubLevel subLevel) {
        this.sable$stubTrackingSubLevel = subLevel;
    }

    @Override
    public void sable$setLastTrackingSubLevelID(final UUID uuid) {
        this.sable$stubLastTrackingId = uuid;
    }

    @Override
    public BlockPos sable$getInBlockStatePos() {
        return this.sable$stubInBlockStatePos;
    }
}
