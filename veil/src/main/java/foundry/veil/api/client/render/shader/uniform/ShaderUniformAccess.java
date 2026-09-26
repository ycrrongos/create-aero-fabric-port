package foundry.veil.api.client.render.shader.uniform;

import org.joml.*;

/**
 * Write access to a single uniform of a {@link foundry.veil.api.client.render.shader.program.ShaderProgram}.
 * Calls on a uniform that does not exist in the program are ignored.
 */
public interface ShaderUniformAccess {

    /**
     * @return Whether this uniform actually exists in the linked program
     */
    boolean isValid();

    void setFloat(float value);

    void setVector(float x, float y);

    void setVector(float x, float y, float z);

    void setVector(float x, float y, float z, float w);

    default void setVector(Vector2fc value) {
        this.setVector(value.x(), value.y());
    }

    default void setVector(Vector3fc value) {
        this.setVector(value.x(), value.y(), value.z());
    }

    default void setVector(Vector4fc value) {
        this.setVector(value.x(), value.y(), value.z(), value.w());
    }

    void setFloats(float... values);

    void setInt(int value);

    void setVectorI(int x, int y);

    void setVectorI(int x, int y, int z);

    void setVectorI(int x, int y, int z, int w);

    void setInts(int... values);

    void setMatrix(Matrix2fc value);

    void setMatrix(Matrix3fc value);

    void setMatrix(Matrix4fc value);
}
