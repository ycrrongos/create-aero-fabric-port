package dev.ryanhcode.sable.mixinhelpers.debug;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

/**
 * A cuboid gizmo with an arbitrary transform, used to show boxes in the local space of sub-levels and rotated entities.
 *
 * @param box       The box in local space
 * @param transform The transform from local space into world space
 * @param style     The style to draw with
 */
public record OrientedBoxGizmo(AABB box, Matrix4dc transform, GizmoStyle style) implements Gizmo {

    /**
     * Adds a box in the plot space of a sub-level.
     */
    public static GizmoProperties inPlot(final AABB plotBox, final Pose3dc pose, final GizmoStyle style) {
        final Matrix4d transform = new Matrix4d()
                .translation(pose.position())
                .rotate(new Quaterniond(pose.orientation()))
                .scale(pose.scale())
                .translate(-pose.rotationPoint().x(), -pose.rotationPoint().y(), -pose.rotationPoint().z());
        return Gizmos.addGizmo(new OrientedBoxGizmo(plotBox, transform, style));
    }

    /**
     * Adds a box rotated around a pivot.
     */
    public static GizmoProperties rotated(final AABB box, final Vec3 pivot, final Quaterniondc orientation, final GizmoStyle style) {
        final Matrix4d transform = new Matrix4d()
                .translation(pivot.x, pivot.y, pivot.z)
                .rotate(orientation)
                .translate(-pivot.x, -pivot.y, -pivot.z);
        return Gizmos.addGizmo(new OrientedBoxGizmo(box, transform, style));
    }

    private Vec3 corner(final double x, final double y, final double z) {
        final Vector3d result = this.transform.transformPosition(new Vector3d(x, y, z));
        return new Vec3(result.x, result.y, result.z);
    }

    @Override
    public void emit(final GizmoPrimitives primitives, final float opacity) {
        final double x0 = this.box.minX;
        final double y0 = this.box.minY;
        final double z0 = this.box.minZ;
        final double x1 = this.box.maxX;
        final double y1 = this.box.maxY;
        final double z1 = this.box.maxZ;

        final Vec3 c000 = this.corner(x0, y0, z0);
        final Vec3 c100 = this.corner(x1, y0, z0);
        final Vec3 c010 = this.corner(x0, y1, z0);
        final Vec3 c110 = this.corner(x1, y1, z0);
        final Vec3 c001 = this.corner(x0, y0, z1);
        final Vec3 c101 = this.corner(x1, y0, z1);
        final Vec3 c011 = this.corner(x0, y1, z1);
        final Vec3 c111 = this.corner(x1, y1, z1);

        if (this.style.hasFill()) {
            final int fill = this.style.multipliedFill(opacity);
            primitives.addQuad(c100, c110, c111, c101, fill);
            primitives.addQuad(c000, c001, c011, c010, fill);
            primitives.addQuad(c000, c010, c110, c100, fill);
            primitives.addQuad(c001, c101, c111, c011, fill);
            primitives.addQuad(c010, c011, c111, c110, fill);
            primitives.addQuad(c000, c100, c101, c001, fill);
        }

        if (this.style.hasStroke()) {
            final int stroke = this.style.multipliedStroke(opacity);
            final float width = this.style.strokeWidth();
            primitives.addLine(c000, c100, stroke, width);
            primitives.addLine(c000, c010, stroke, width);
            primitives.addLine(c000, c001, stroke, width);
            primitives.addLine(c100, c110, stroke, width);
            primitives.addLine(c110, c010, stroke, width);
            primitives.addLine(c010, c011, stroke, width);
            primitives.addLine(c011, c001, stroke, width);
            primitives.addLine(c001, c101, stroke, width);
            primitives.addLine(c101, c100, stroke, width);
            primitives.addLine(c011, c111, stroke, width);
            primitives.addLine(c101, c111, stroke, width);
            primitives.addLine(c110, c111, stroke, width);
        }
    }
}
