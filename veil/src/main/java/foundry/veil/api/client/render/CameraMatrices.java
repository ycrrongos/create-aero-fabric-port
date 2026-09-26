package foundry.veil.api.client.render;

import org.joml.*;

/**
 * The camera matrices of the level currently being rendered.
 */
public class CameraMatrices {

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f inverseProjectionMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f inverseViewMatrix = new Matrix4f();
    private final Matrix3f inverseViewRotMatrix = new Matrix3f();
    private final Vector3f cameraPosition = new Vector3f();
    private final Vector3f cameraBobOffset = new Vector3f();
    private float nearPlane;
    private float farPlane;

    private void extractPlanes() {
        if ((this.projectionMatrix.properties() & Matrix4fc.PROPERTY_PERSPECTIVE) != 0) {
            this.nearPlane = this.projectionMatrix.perspectiveNear();
            this.farPlane = this.projectionMatrix.perspectiveFar();
        } else {
            Vector3f temp = new Vector3f();
            this.nearPlane = this.inverseProjectionMatrix.transformPosition(0, 0, -1, temp).z();
            this.farPlane = this.inverseProjectionMatrix.transformPosition(0, 0, 1, temp).z();
        }
    }

    /**
     * Updates all matrices for a level render.
     *
     * @param projection The projection matrix
     * @param modelView  The view matrix
     * @param x          The camera x position
     * @param y          The camera y position
     * @param z          The camera z position
     */
    public void update(Matrix4fc projection, Matrix4fc modelView, double x, double y, double z) {
        this.projectionMatrix.set(projection);
        this.projectionMatrix.invert(this.inverseProjectionMatrix);
        this.viewMatrix.set(modelView);
        this.viewMatrix.invert(this.inverseViewMatrix);
        this.inverseViewMatrix.normal(this.inverseViewRotMatrix);
        this.cameraPosition.set(x, y, z);
        this.cameraBobOffset.set(0);
        this.extractPlanes();
    }

    public void backup(CameraMatrices store) {
        store.projectionMatrix.set(this.projectionMatrix);
        store.inverseProjectionMatrix.set(this.inverseProjectionMatrix);
        store.viewMatrix.set(this.viewMatrix);
        store.inverseViewMatrix.set(this.inverseViewMatrix);
        store.inverseViewRotMatrix.set(this.inverseViewRotMatrix);
        store.cameraPosition.set(this.cameraPosition);
        store.cameraBobOffset.set(this.cameraBobOffset);
        store.nearPlane = this.nearPlane;
        store.farPlane = this.farPlane;
    }

    public void restore(CameraMatrices load) {
        load.backup(this);
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f getInverseProjectionMatrix() {
        return this.inverseProjectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }

    public Matrix4f getInverseViewMatrix() {
        return this.inverseViewMatrix;
    }

    public Matrix3f getInverseViewRotMatrix() {
        return this.inverseViewRotMatrix;
    }

    public Vector3f getCameraPosition() {
        return this.cameraPosition;
    }

    public Vector3f getCameraBobOffset() {
        return this.cameraBobOffset;
    }

    public float getNearPlane() {
        return this.nearPlane;
    }

    public float getFarPlane() {
        return this.farPlane;
    }
}
