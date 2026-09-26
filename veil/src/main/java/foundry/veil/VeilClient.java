package foundry.veil;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.rendertype.VeilBlockLayers;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.fabric.event.FabricFreeNativeResourcesEvent;
import foundry.veil.fabric.event.FabricVeilRegisterBlockLayersEvent;
import foundry.veil.fabric.event.FabricVeilRegisterFixedBuffersEvent;
import foundry.veil.fabric.event.FabricVeilRendererAvailableEvent;
import foundry.veil.impl.client.render.VeilLevelRenderStages;
import foundry.veil.mixin.client.BufferSourceAccessor;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.SequencedMap;

/**
 * Client side initialization of Veil.
 */
@ApiStatus.Internal
public final class VeilClient {

    private VeilClient() {
    }

    /**
     * Called from the Minecraft constructor once the render system is ready and before the first resource reload.
     */
    public static void initRenderer(Minecraft minecraft, ReloadableResourceManager resourceManager) {
        VeilRenderSystem.init();
        VeilRenderer renderer = VeilRenderSystem.renderer();
        renderer.getShaderPreProcessors().reload(resourceManager);

        resourceManager.registerReloadListener(renderer.getFramebufferManager());
        resourceManager.registerReloadListener(renderer.getShaderManager());
        resourceManager.registerReloadListener(renderer.getPostProcessingManager());

        FabricVeilRendererAvailableEvent.EVENT.invoker().onVeilRendererAvailable(renderer);
        FabricVeilRegisterBlockLayersEvent.EVENT.invoker().onRegisterBlockLayers(renderType -> {
            if (!(renderType instanceof VeilRenderType veilRenderType)) {
                throw new IllegalArgumentException("Block layers must be Veil render types: " + renderType);
            }
            VeilBlockLayers.register(veilRenderType);
        });
        FabricVeilRegisterFixedBuffersEvent.EVENT.invoker().onRegisterFixedBuffers((stage, renderType) -> {
            SequencedMap<net.minecraft.client.renderer.rendertype.RenderType, ByteBufferBuilder> fixedBuffers = ((BufferSourceAccessor) minecraft.renderBuffers().bufferSource()).veil$getFixedBuffers();
            ByteBufferBuilder old = fixedBuffers.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
            if (old != null) {
                old.close();
            }
            VeilLevelRenderStages.registerFixedBuffer(stage, renderType);
        });
    }

    public static void close() {
        FabricFreeNativeResourcesEvent.EVENT.invoker().onFree();
        if (VeilRenderSystem.isRendererAvailable()) {
            VeilRenderSystem.renderer().free();
        }
    }
}
