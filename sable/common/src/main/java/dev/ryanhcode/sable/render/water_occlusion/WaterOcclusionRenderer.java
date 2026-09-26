package dev.ryanhcode.sable.render.water_occlusion;

import dev.ryanhcode.sable.render.region.SimpleCulledRenderRegion;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Stubbed for 1.21.11 spike — water occlusion needs RenderPipeline rewrite.
 */
@ApiStatus.Internal
public class WaterOcclusionRenderer {

    private static boolean isEnabled = false;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void setIsEnabled(final boolean isEnabled) {
        WaterOcclusionRenderer.isEnabled = isEnabled;
    }

    @Nullable
    @ApiStatus.Internal
    public SimpleCulledRenderRegion addRegion(final Collection<BlockPos> blocks) {
        return null;
    }

    @ApiStatus.Internal
    public void removeRegion(final SimpleCulledRenderRegion region) {
    }
}
