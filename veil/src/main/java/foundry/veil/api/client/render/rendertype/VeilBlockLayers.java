package foundry.veil.api.client.render.rendertype;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Custom chunk layers. A block assigned to extra layers is compiled into its normal vanilla chunk layer <em>and</em> into
 * every extra layer; each extra layer is then drawn with its own {@link VeilRenderType} at the stage it was registered
 * for (see {@link foundry.veil.api.event.VeilRegisterFixedBuffersEvent}).
 */
public final class VeilBlockLayers {

    private static final List<VeilRenderType> LAYERS = new ArrayList<>();
    private static final Map<Block, List<VeilRenderType>> EXTRA_LAYERS = new IdentityHashMap<>();

    private VeilBlockLayers() {
    }

    /**
     * Registers a render type as a chunk layer.
     */
    public static synchronized void register(VeilRenderType layer) {
        if (!LAYERS.contains(layer)) {
            LAYERS.add(layer);
        }
    }

    /**
     * Makes the specified block render into the extra layers, in addition to its normal chunk layer.
     */
    public static synchronized void setExtraLayers(Block block, VeilRenderType... layers) {
        for (VeilRenderType layer : layers) {
            register(layer);
        }
        EXTRA_LAYERS.put(block, List.of(layers));
    }

    public static List<VeilRenderType> getExtraLayers(BlockState state) {
        List<VeilRenderType> layers = EXTRA_LAYERS.get(state.getBlock());
        return layers != null ? layers : List.of();
    }

    public static boolean hasExtraLayers() {
        return !EXTRA_LAYERS.isEmpty();
    }

    public static List<VeilRenderType> getLayers() {
        return Collections.unmodifiableList(LAYERS);
    }

    public static boolean isBlockLayer(VeilRenderType renderType) {
        return LAYERS.contains(renderType);
    }
}
