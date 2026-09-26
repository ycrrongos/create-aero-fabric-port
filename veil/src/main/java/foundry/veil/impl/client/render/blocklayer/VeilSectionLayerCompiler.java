package foundry.veil.impl.client.render.blocklayer;

import com.mojang.blaze3d.vertex.*;
import foundry.veil.api.client.render.rendertype.VeilBlockLayers;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compiles the extra block layers of a chunk section on the section compile thread.
 */
@ApiStatus.Internal
public final class VeilSectionLayerCompiler {

    private static final ThreadLocal<Map<VeilRenderType, ByteBufferBuilder>> BUFFERS = ThreadLocal.withInitial(IdentityHashMap::new);
    private static final ThreadLocal<Map<VeilRenderType, BufferBuilder>> BUILDERS = ThreadLocal.withInitial(IdentityHashMap::new);

    private VeilSectionLayerCompiler() {
    }

    /**
     * Renders a block into its extra layers.
     */
    public static void renderBlock(BlockRenderDispatcher blockRenderer, BlockState state, BlockPos pos, RenderSectionRegion region, PoseStack poseStack, List<BlockModelPart> parts) {
        List<VeilRenderType> layers = VeilBlockLayers.getExtraLayers(state);
        if (layers.isEmpty()) {
            return;
        }
        Map<VeilRenderType, BufferBuilder> builders = BUILDERS.get();
        for (VeilRenderType layer : layers) {
            BufferBuilder builder = builders.get(layer);
            if (builder == null) {
                ByteBufferBuilder buffer = BUFFERS.get().computeIfAbsent(layer, unused -> new ByteBufferBuilder(DefaultVertexFormat.BLOCK.getVertexSize() * 4 * 256));
                builder = new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                builders.put(layer, builder);
            }
            blockRenderer.renderBatched(state, pos, region, poseStack, builder, true, parts);
        }
    }

    /**
     * Finishes all extra layers of the section being compiled.
     *
     * @return The compiled layer data or <code>null</code> if the section has no extra layer geometry
     */
    public static @Nullable VeilSectionLayerData finish() {
        Map<VeilRenderType, BufferBuilder> builders = BUILDERS.get();
        if (builders.isEmpty()) {
            return null;
        }
        VeilSectionLayerData data = null;
        for (Map.Entry<VeilRenderType, BufferBuilder> entry : builders.entrySet()) {
            try (MeshData meshData = entry.getValue().build()) {
                if (meshData != null) {
                    if (data == null) {
                        data = new VeilSectionLayerData();
                    }
                    data.add(entry.getKey(), meshData.vertexBuffer(), meshData.drawState().vertexCount());
                }
            }
        }
        builders.clear();
        return data;
    }

    /**
     * Discards any unfinished layer of the current thread.
     */
    public static void reset() {
        Map<VeilRenderType, BufferBuilder> builders = BUILDERS.get();
        for (BufferBuilder builder : builders.values()) {
            try (MeshData meshData = builder.build()) {
                // discard
            }
        }
        builders.clear();
    }
}
