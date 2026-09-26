package foundry.veil.platform;

import foundry.veil.api.event.VeilAddShaderPreProcessorsEvent;
import foundry.veil.api.event.VeilPostProcessingEvent;
import foundry.veil.api.event.VeilShaderCompileEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

/**
 * Invokes client events on the current platform.
 */
@ApiStatus.Internal
public interface VeilClientPlatform extends
        VeilPostProcessingEvent.Pre,
        VeilPostProcessingEvent.Post,
        VeilAddShaderPreProcessorsEvent,
        VeilShaderCompileEvent {

    VeilClientPlatform INSTANCE = ServiceLoader.load(VeilClientPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find Veil client platform"));
}
