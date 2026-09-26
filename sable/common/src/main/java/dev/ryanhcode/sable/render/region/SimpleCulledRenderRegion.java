package dev.ryanhcode.sable.render.region;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import java.util.Collection;

/**
 * Stubbed for 1.21.11 spike — VertexBuffer path removed.
 */
@ApiStatus.Internal
public abstract class SimpleCulledRenderRegion {

    public SimpleCulledRenderRegion(final Collection<BlockPos> blocks) {
    }

    public void render(final Matrix4f modelView, final Matrix4f projectionMatrix) {
    }

    public void free() {
    }

    public abstract SimpleCulledRenderRegionBuilder createMeshBuilder(int gridSize);
}
