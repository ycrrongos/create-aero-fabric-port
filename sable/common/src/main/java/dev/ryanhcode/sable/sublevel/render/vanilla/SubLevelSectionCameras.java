package dev.ryanhcode.sable.sublevel.render.vanilla;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread safe lookup of the camera position in the plot space of sub-levels.
 * <p>
 * Section compilation and translucency sorting run on worker threads and use the camera position to sort geometry and
 * prioritize work. Sub-level sections live in plot space, so they need the camera transformed into that space.
 */
@ApiStatus.Internal
public final class SubLevelSectionCameras {

    private static final Map<Long, VanillaChunkedSubLevelRenderData> BY_CHUNK = new ConcurrentHashMap<>();

    private SubLevelSectionCameras() {
    }

    static void register(final int chunkX, final int chunkZ, final VanillaChunkedSubLevelRenderData data) {
        BY_CHUNK.put(ChunkPos.asLong(chunkX, chunkZ), data);
    }

    static void unregister(final int chunkX, final int chunkZ, final VanillaChunkedSubLevelRenderData data) {
        BY_CHUNK.remove(ChunkPos.asLong(chunkX, chunkZ), data);
    }

    /**
     * @param sectionNode A packed section position
     * @return The camera position in the plot space of the sub-level owning the section, or null if the section isn't part of a sub-level
     */
    public static @Nullable Vec3 getLocalCamera(final long sectionNode) {
        return getLocalCamera(SectionPos.x(sectionNode), SectionPos.z(sectionNode));
    }

    /**
     * @return The camera position in the plot space of the sub-level owning the chunk, or null if the chunk isn't part of a sub-level
     */
    public static @Nullable Vec3 getLocalCamera(final int chunkX, final int chunkZ) {
        final VanillaChunkedSubLevelRenderData data = BY_CHUNK.get(ChunkPos.asLong(chunkX, chunkZ));
        return data != null ? data.getLocalCamera() : null;
    }

    public static void clear() {
        BY_CHUNK.clear();
    }
}
