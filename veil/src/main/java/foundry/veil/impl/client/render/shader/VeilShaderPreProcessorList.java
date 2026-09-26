package foundry.veil.impl.client.render.shader;

import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import foundry.veil.api.event.VeilAddShaderPreProcessorsEvent;
import foundry.veil.platform.VeilClientPlatform;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds every shader pre-processor registered through {@link VeilAddShaderPreProcessorsEvent}.
 */
@ApiStatus.Internal
public final class VeilShaderPreProcessorList {

    private final List<ShaderPreProcessor> processors = new ArrayList<>();
    private ShaderPreProcessor processor = ShaderPreProcessor.NOOP;
    private boolean loaded;

    /**
     * Collects all pre-processors again. Called on resource reload.
     */
    public void reload(ResourceProvider resourceProvider) {
        this.processors.clear();
        VeilClientPlatform.INSTANCE.onRegisterShaderPreProcessors(resourceProvider, new VeilAddShaderPreProcessorsEvent.Registry() {
            @Override
            public void addPreprocessorFirst(ShaderPreProcessor processor, boolean modifyImports) {
                VeilShaderPreProcessorList.this.processors.addFirst(processor);
            }

            @Override
            public void addPreprocessor(ShaderPreProcessor processor, boolean modifyImports) {
                VeilShaderPreProcessorList.this.processors.add(processor);
            }
        });
        this.processor = ShaderPreProcessor.allOf(this.processors);
        this.loaded = true;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean isEmpty() {
        return this.processor == ShaderPreProcessor.NOOP;
    }

    public ShaderPreProcessor processor() {
        return this.processor;
    }
}
