package foundry.veil.impl.client.render.blocklayer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented on compile results and compiled meshes to carry extra block layer geometry.
 */
@ApiStatus.Internal
public interface VeilSectionLayerHolder {

    @Nullable
    VeilSectionLayerData veil$getLayerData();

    void veil$setLayerData(@Nullable VeilSectionLayerData data);
}
