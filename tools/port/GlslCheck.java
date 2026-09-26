import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20C;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compiles (and optionally links) GLSL shaders with the local OpenGL driver to validate rewritten shader sources.
 * Usage: java -cp lwjgl... GlslCheck.java [vertex.vsh] [fragment.fsh]
 * Needs a display (e.g. Xvfb).
 */
public class GlslCheck {
    public static void main(String[] args) throws Exception {
        if (!GLFW.glfwInit()) throw new IllegalStateException("glfwInit failed");
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        long window = GLFW.glfwCreateWindow(16, 16, "glslcheck", 0, 0);
        if (window == 0) throw new IllegalStateException("window creation failed");
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        System.out.println("GL_RENDERER: " + GL20C.glGetString(GL20C.GL_RENDERER) + " / " + GL20C.glGetString(GL20C.GL_VERSION));
        int program = GL20C.glCreateProgram();
        boolean ok = true;
        for (String file : args) {
            int type = file.endsWith(".vsh") ? GL20C.GL_VERTEX_SHADER : GL20C.GL_FRAGMENT_SHADER;
            int shader = GL20C.glCreateShader(type);
            GL20C.glShaderSource(shader, Files.readString(Path.of(file)));
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0) {
                ok = false;
                System.out.println(file + ": COMPILE FAILED\n" + GL20C.glGetShaderInfoLog(shader));
            } else {
                System.out.println(file + ": compiled");
            }
            GL20C.glAttachShader(program, shader);
        }
        if (ok && args.length > 1) {
            GL20C.glLinkProgram(program);
            if (GL20C.glGetProgrami(program, GL20C.GL_LINK_STATUS) == 0) {
                ok = false;
                System.out.println("LINK FAILED\n" + GL20C.glGetProgramInfoLog(program));
            } else {
                System.out.println("linked");
            }
        }
        GLFW.glfwTerminate();
        System.exit(ok ? 0 : 1);
    }
}
