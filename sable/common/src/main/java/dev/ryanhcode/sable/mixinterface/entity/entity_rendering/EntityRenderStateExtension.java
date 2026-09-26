package dev.ryanhcode.sable.mixinterface.entity.entity_rendering;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Sub-level related render data of an entity, captured when its render state is extracted.
 */
public interface EntityRenderStateExtension {

    /**
     * @return The orientation of the sub-level the entity is inside of, rotated around the entity origin when submitting
     */
    @Nullable Quaternionf sable$getSubLevelOrientation();

    void sable$setSubLevelOrientation(@Nullable Quaternionf orientation);

    /**
     * @return The custom orientation of the entity model from sub-levels the entity stands on or rides
     */
    @Nullable Quaternionf sable$getModelOrientation();

    /**
     * @return The offset of the entity model from its render position, applied before rotating
     */
    Vector3f sable$getModelOffset();

    /**
     * @return The point the entity model is rotated around, relative to its render position
     */
    Vector3f sable$getModelPivot();

    void sable$setModelOrientation(@Nullable Quaternionf orientation, double offsetX, double offsetY, double offsetZ, double pivotX, double pivotY, double pivotZ);

    /**
     * @return Vertices of the shadow the entity casts on sub-levels, relative to the entity origin, packed as
     * {@code x, y, z, u, v, alpha} per vertex
     */
    float @Nullable [] sable$getSubLevelShadow();

    void sable$setSubLevelShadow(float @Nullable [] vertices);
}
