package foundry.veil.api.client.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A culling frustum wrapper around the vanilla {@link Frustum}.
 */
public interface CullFrustum {

    /**
     * @return The camera position the frustum was prepared for
     */
    Vector3dc getPosition();

    boolean testAab(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    default boolean testAab(AABB aabb) {
        return this.testAab(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    boolean testPoint(double x, double y, double z);

    /**
     * @return The underlying vanilla frustum
     */
    Frustum toFrustum();

    static CullFrustum of(Frustum frustum) {
        return new CullFrustum() {
            private final Vector3d position = new Vector3d(frustum.getCamX(), frustum.getCamY(), frustum.getCamZ());

            @Override
            public Vector3dc getPosition() {
                return this.position.set(frustum.getCamX(), frustum.getCamY(), frustum.getCamZ());
            }

            @Override
            public boolean testAab(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
                return frustum.isVisible(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
            }

            @Override
            public boolean testPoint(double x, double y, double z) {
                return frustum.pointInFrustum(x, y, z);
            }

            @Override
            public Frustum toFrustum() {
                return frustum;
            }
        };
    }
}
