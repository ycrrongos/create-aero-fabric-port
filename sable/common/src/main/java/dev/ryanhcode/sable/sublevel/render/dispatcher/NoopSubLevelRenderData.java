package dev.ryanhcode.sable.sublevel.render.dispatcher;

import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import net.minecraft.client.Camera;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.chunk.RenderRegionCache;

/**
 * Compile-spike placeholder until the 1.21.11 RenderPipeline port lands.
 */
public final class NoopSubLevelRenderData implements SubLevelRenderData {

    private final ClientSubLevel subLevel;

    public NoopSubLevelRenderData(final ClientSubLevel subLevel) {
        this.subLevel = subLevel;
    }

    @Override
    public void close() {
    }

    @Override
    public void rebuild() {
    }

    @Override
    public boolean isSectionCompiled(final int x, final int y, final int z) {
        return false;
    }

    @Override
    public void setDirty(final int x, final int y, final int z, final boolean playerChanged) {
    }

    @Override
    public void compileSections(final PrioritizeChunkUpdates chunkUpdates, final RenderRegionCache renderRegionCache, final Camera camera) {
    }

    @Override
    public int getVisibleSectionCount() {
        return 0;
    }

    @Override
    public ClientSubLevel getSubLevel() {
        return this.subLevel;
    }
}
