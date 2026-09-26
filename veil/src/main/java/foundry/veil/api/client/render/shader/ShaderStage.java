package foundry.veil.api.client.render.shader;

import com.mojang.blaze3d.shaders.ShaderType;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

/**
 * A programmable stage of an OpenGL shader program.
 */
public enum ShaderStage {
    VERTEX("vertex", ".vsh", GL_VERTEX_SHADER),
    TESS_CONTROL("tesselation_control", ".tcsh", GL_TESS_CONTROL_SHADER),
    TESS_EVALUATION("tesselation_evaluation", ".tesh", GL_TESS_EVALUATION_SHADER),
    GEOMETRY("geometry", ".gsh", GL_GEOMETRY_SHADER),
    FRAGMENT("fragment", ".fsh", GL_FRAGMENT_SHADER),
    COMPUTE("compute", ".comp", GL_COMPUTE_SHADER);

    private final String jsonKey;
    private final String extension;
    private final int glType;

    ShaderStage(String jsonKey, String extension, int glType) {
        this.jsonKey = jsonKey;
        this.extension = extension;
        this.glType = glType;
    }

    public String getJsonKey() {
        return this.jsonKey;
    }

    public String getExtension() {
        return this.extension;
    }

    public int getGlType() {
        return this.glType;
    }

    public static @Nullable ShaderStage fromVanilla(ShaderType type) {
        return switch (type) {
            case VERTEX -> VERTEX;
            case FRAGMENT -> FRAGMENT;
        };
    }
}
