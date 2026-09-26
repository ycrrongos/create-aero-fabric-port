package foundry.veil.platform;

import foundry.veil.api.event.*;

import java.util.ServiceLoader;

/**
 * Registers listeners for Veil events on the current platform.
 */
public interface VeilEventPlatform {

    VeilEventPlatform INSTANCE = ServiceLoader.load(VeilEventPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find platform event provider"));

    void onFreeNativeResources(FreeNativeResourcesEvent event);

    void onVeilAddShaderProcessors(VeilAddShaderPreProcessorsEvent event);

    void preVeilPostProcessing(VeilPostProcessingEvent.Pre event);

    void postVeilPostProcessing(VeilPostProcessingEvent.Post event);

    void onVeilRegisterBlockLayers(VeilRegisterBlockLayersEvent event);

    void onVeilRegisterFixedBuffers(VeilRegisterFixedBuffersEvent event);

    void onVeilRendererAvailable(VeilRendererAvailableEvent event);

    void onVeilRenderLevelStage(VeilRenderLevelStageEvent event);

    void onVeilShaderCompile(VeilShaderCompileEvent event);
}
