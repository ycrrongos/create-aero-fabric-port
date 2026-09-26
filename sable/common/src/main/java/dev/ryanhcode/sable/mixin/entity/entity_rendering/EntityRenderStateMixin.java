package dev.ryanhcode.sable.mixin.entity.entity_rendering;

import dev.ryanhcode.sable.mixinterface.entity.entity_rendering.EntityRenderStateExtension;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateExtension {

    @Unique
    private @Nullable Quaternionf sable$subLevelOrientation;

    @Unique
    private @Nullable Quaternionf sable$modelOrientation;

    @Unique
    private final Vector3f sable$modelOffset = new Vector3f();

    @Unique
    private final Vector3f sable$modelPivot = new Vector3f();

    @Unique
    private float @Nullable [] sable$subLevelShadow;

    @Unique
    private @Nullable Quaternionf sable$sleepingOrientation;

    @Override
    public @Nullable Quaternionf sable$getSubLevelOrientation() {
        return this.sable$subLevelOrientation;
    }

    @Override
    public void sable$setSubLevelOrientation(@Nullable final Quaternionf orientation) {
        this.sable$subLevelOrientation = orientation;
    }

    @Override
    public @Nullable Quaternionf sable$getModelOrientation() {
        return this.sable$modelOrientation;
    }

    @Override
    public Vector3f sable$getModelOffset() {
        return this.sable$modelOffset;
    }

    @Override
    public Vector3f sable$getModelPivot() {
        return this.sable$modelPivot;
    }

    @Override
    public void sable$setModelOrientation(@Nullable final Quaternionf orientation, final double offsetX, final double offsetY, final double offsetZ, final double pivotX, final double pivotY, final double pivotZ) {
        this.sable$modelOrientation = orientation;
        this.sable$modelOffset.set((float) offsetX, (float) offsetY, (float) offsetZ);
        this.sable$modelPivot.set((float) pivotX, (float) pivotY, (float) pivotZ);
    }

    @Override
    public float @Nullable [] sable$getSubLevelShadow() {
        return this.sable$subLevelShadow;
    }

    @Override
    public void sable$setSubLevelShadow(final float @Nullable [] vertices) {
        this.sable$subLevelShadow = vertices;
    }

    @Override
    public @Nullable Quaternionf sable$getSleepingOrientation() {
        return this.sable$sleepingOrientation;
    }

    @Override
    public void sable$setSleepingOrientation(@Nullable final Quaternionf orientation) {
        this.sable$sleepingOrientation = orientation;
    }
}
