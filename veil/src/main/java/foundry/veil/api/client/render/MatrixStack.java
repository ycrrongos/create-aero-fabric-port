package foundry.veil.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

/**
 * A matrix stack abstraction backed by a vanilla {@link PoseStack}.
 */
public interface MatrixStack {

    void pushMatrix();

    void popMatrix();

    /**
     * Clears the stack back to a single identity matrix.
     */
    void clear();

    boolean isEmpty();

    void setIdentity();

    void translate(double x, double y, double z);

    void rotate(Quaternionfc rotation);

    void scale(float x, float y, float z);

    Matrix4f position();

    Matrix3f normal();

    PoseStack toPoseStack();

    static MatrixStack of(PoseStack poseStack) {
        return new MatrixStack() {
            @Override
            public void pushMatrix() {
                poseStack.pushPose();
            }

            @Override
            public void popMatrix() {
                poseStack.popPose();
            }

            @Override
            public void clear() {
                while (!poseStack.isEmpty()) {
                    poseStack.popPose();
                }
                poseStack.setIdentity();
            }

            @Override
            public boolean isEmpty() {
                return poseStack.isEmpty();
            }

            @Override
            public void setIdentity() {
                poseStack.setIdentity();
            }

            @Override
            public void translate(double x, double y, double z) {
                poseStack.translate(x, y, z);
            }

            @Override
            public void rotate(Quaternionfc rotation) {
                poseStack.mulPose(rotation);
            }

            @Override
            public void scale(float x, float y, float z) {
                poseStack.scale(x, y, z);
            }

            @Override
            public Matrix4f position() {
                return poseStack.last().pose();
            }

            @Override
            public Matrix3f normal() {
                return poseStack.last().normal();
            }

            @Override
            public PoseStack toPoseStack() {
                return poseStack;
            }
        };
    }
}
