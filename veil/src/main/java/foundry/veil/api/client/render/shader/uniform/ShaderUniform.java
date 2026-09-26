package foundry.veil.api.client.render.shader.uniform;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.VeilGlStateSync;
import org.joml.Matrix2fc;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL41C.*;

/**
 * A uniform location inside a linked Veil program.
 */
public final class ShaderUniform implements ShaderUniformAccess {

    public static final ShaderUniform INVALID = new ShaderUniform(0, -1, "invalid");

    private final int program;
    private final int location;
    private final String name;

    public ShaderUniform(int program, int location, String name) {
        this.program = program;
        this.location = location;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getLocation() {
        return this.location;
    }

    @Override
    public boolean isValid() {
        return this.location != -1;
    }

    private boolean direct() {
        return VeilRenderSystem.separateShaderObjectsSupported();
    }

    private void bindProgram() {
        VeilGlStateSync.useProgram(this.program);
    }

    @Override
    public void setFloat(float value) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform1f(this.program, this.location, value);
        } else {
            this.bindProgram();
            glUniform1f(this.location, value);
        }
    }

    @Override
    public void setVector(float x, float y) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform2f(this.program, this.location, x, y);
        } else {
            this.bindProgram();
            glUniform2f(this.location, x, y);
        }
    }

    @Override
    public void setVector(float x, float y, float z) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform3f(this.program, this.location, x, y, z);
        } else {
            this.bindProgram();
            glUniform3f(this.location, x, y, z);
        }
    }

    @Override
    public void setVector(float x, float y, float z, float w) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform4f(this.program, this.location, x, y, z, w);
        } else {
            this.bindProgram();
            glUniform4f(this.location, x, y, z, w);
        }
    }

    @Override
    public void setFloats(float... values) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform1fv(this.program, this.location, values);
        } else {
            this.bindProgram();
            glUniform1fv(this.location, values);
        }
    }

    @Override
    public void setInt(int value) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform1i(this.program, this.location, value);
        } else {
            this.bindProgram();
            glUniform1i(this.location, value);
        }
    }

    @Override
    public void setVectorI(int x, int y) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform2i(this.program, this.location, x, y);
        } else {
            this.bindProgram();
            glUniform2i(this.location, x, y);
        }
    }

    @Override
    public void setVectorI(int x, int y, int z) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform3i(this.program, this.location, x, y, z);
        } else {
            this.bindProgram();
            glUniform3i(this.location, x, y, z);
        }
    }

    @Override
    public void setVectorI(int x, int y, int z, int w) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform4i(this.program, this.location, x, y, z, w);
        } else {
            this.bindProgram();
            glUniform4i(this.location, x, y, z, w);
        }
    }

    @Override
    public void setInts(int... values) {
        if (this.location == -1) return;
        if (this.direct()) {
            glProgramUniform1iv(this.program, this.location, values);
        } else {
            this.bindProgram();
            glUniform1iv(this.location, values);
        }
    }

    @Override
    public void setMatrix(Matrix2fc value) {
        if (this.location == -1) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = value.get(stack.mallocFloat(4));
            if (this.direct()) {
                glProgramUniformMatrix2fv(this.program, this.location, false, buffer);
            } else {
                this.bindProgram();
                glUniformMatrix2fv(this.location, false, buffer);
            }
        }
    }

    @Override
    public void setMatrix(Matrix3fc value) {
        if (this.location == -1) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = value.get(stack.mallocFloat(9));
            if (this.direct()) {
                glProgramUniformMatrix3fv(this.program, this.location, false, buffer);
            } else {
                this.bindProgram();
                glUniformMatrix3fv(this.location, false, buffer);
            }
        }
    }

    @Override
    public void setMatrix(Matrix4fc value) {
        if (this.location == -1) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = value.get(stack.mallocFloat(16));
            if (this.direct()) {
                glProgramUniformMatrix4fv(this.program, this.location, false, buffer);
            } else {
                this.bindProgram();
                glUniformMatrix4fv(this.location, false, buffer);
            }
        }
    }
}
