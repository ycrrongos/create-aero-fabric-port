package dev.ryanhcode.sable.mixinhelpers.block_outline_render;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A camera viewing the world from the local (plot) space of a sub-level.
 * With no pose set, it mirrors the render camera.
 */
@ApiStatus.Internal
public class SubLevelCamera extends Camera {

    private Camera renderCamera;
    private boolean hasPose;
    private final Quaterniond inverseOrientation = new Quaterniond();
    private final Quaternionf inverseOrientationf = new Quaternionf();
    private final Quaternionf localRotation = new Quaternionf();
    private final Vector3f rotationYXZ = new Vector3f();
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, -1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(-1.0F, 0.0F, 0.0F);

    private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
    private Vec3 pos = Vec3.ZERO;

    public void setCamera(final Camera renderCamera) {
        this.renderCamera = renderCamera;
    }

    public void setPose(@Nullable final Pose3dc pose) {
        this.hasPose = pose != null;
        if (pose != null) {
            this.pos = pose.transformPositionInverse(this.renderCamera.position());
            this.renderCamera.rotation().mul(this.inverseOrientationf.set(pose.orientation().invert(this.inverseOrientation)), this.localRotation);
        } else {
            this.pos = this.renderCamera.position();
            this.localRotation.set(this.renderCamera.rotation());
        }

        this.blockPosition.set(this.pos.x, this.pos.y, this.pos.z);
        this.localRotation.getEulerAnglesYXZ(this.rotationYXZ);

        this.forwards.set(0.0F, 0.0F, -1.0F).rotate(this.localRotation);
        this.up.set(0.0F, 1.0F, 0.0F).rotate(this.localRotation);
        this.left.set(-1.0F, 0.0F, 0.0F).rotate(this.localRotation);
    }

    public void clear() {
        this.renderCamera = null;
        this.hasPose = false;
        this.pos = Vec3.ZERO;
    }

    @Override
    public @NotNull Vec3 position() {
        return this.pos;
    }

    @Override
    public @NotNull BlockPos blockPosition() {
        return this.blockPosition;
    }

    @Override
    public float xRot() {
        return this.hasPose ? (float) (180.0 / Math.PI * -this.rotationYXZ.x) : this.renderCamera.xRot();
    }

    @Override
    public float yRot() {
        return this.hasPose ? (float) (180.0 / Math.PI * -this.rotationYXZ.y + 180.0) : this.renderCamera.yRot();
    }

    @Override
    public @NotNull Quaternionf rotation() {
        return this.localRotation;
    }

    @Override
    public @NotNull Vector3fc forwardVector() {
        return this.forwards;
    }

    @Override
    public @NotNull Vector3fc upVector() {
        return this.up;
    }

    @Override
    public @NotNull Vector3fc leftVector() {
        return this.left;
    }

    @Override
    public @NotNull Entity entity() {
        return this.renderCamera.entity();
    }

    @Override
    public boolean isInitialized() {
        return this.renderCamera.isInitialized();
    }

    @Override
    public boolean isDetached() {
        return this.renderCamera.isDetached();
    }

    @Override
    public @NotNull EnvironmentAttributeProbe attributeProbe() {
        return this.renderCamera.attributeProbe();
    }

    @Override
    public @NotNull NearPlane getNearPlane() {
        return this.renderCamera.getNearPlane();
    }

    @Override
    public @NotNull FogType getFluidInCamera() {
        return this.renderCamera.getFluidInCamera();
    }

    @Override
    public void reset() {
        this.renderCamera.reset();
    }

    @Override
    public float getPartialTickTime() {
        return this.renderCamera.getPartialTickTime();
    }

    public Camera getRenderCamera() {
        return this.renderCamera;
    }
}
