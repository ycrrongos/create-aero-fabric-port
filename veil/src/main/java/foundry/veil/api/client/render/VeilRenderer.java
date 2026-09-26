package foundry.veil.api.client.render;

import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.ShaderPreDefinitions;
import foundry.veil.impl.client.render.shader.VeilShaderPreProcessorList;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

/**
 * Holds all Veil render managers.
 */
public class VeilRenderer implements NativeResource {

    private final ShaderPreDefinitions shaderDefinitions;
    private final VeilShaderPreProcessorList shaderPreProcessors;
    private final ShaderManager shaderManager;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;
    private final CameraMatrices cameraMatrices;

    @ApiStatus.Internal
    public VeilRenderer() {
        this.shaderDefinitions = new ShaderPreDefinitions();
        this.shaderPreProcessors = new VeilShaderPreProcessorList();
        this.shaderManager = new ShaderManager(this.shaderDefinitions, this.shaderPreProcessors);
        this.framebufferManager = new FramebufferManager();
        this.postProcessingManager = new PostProcessingManager();
        this.cameraMatrices = new CameraMatrices();
    }

    public ShaderPreDefinitions getShaderDefinitions() {
        return this.shaderDefinitions;
    }

    public VeilShaderPreProcessorList getShaderPreProcessors() {
        return this.shaderPreProcessors;
    }

    public ShaderManager getShaderManager() {
        return this.shaderManager;
    }

    public FramebufferManager getFramebufferManager() {
        return this.framebufferManager;
    }

    public PostProcessingManager getPostProcessingManager() {
        return this.postProcessingManager;
    }

    public CameraMatrices getCameraMatrices() {
        return this.cameraMatrices;
    }

    @Override
    public void free() {
        this.shaderManager.free();
        this.framebufferManager.free();
        this.postProcessingManager.free();
    }
}
