package dev.ryanhcode.sable.fabric.platform;

import dev.ryanhcode.sable.platform.SablePlotPlatform;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
public class SablePlotPlatformImpl implements SablePlotPlatform {

    @Override
    public void readLightData(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk) {
        // no-op
    }

    @Override
    public void readChunkAttachments(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk) {
        // AttachmentTargetImpl API changed in Fabric API for 1.21.11 — deferred
    }

    @Override
    public void postLoad(final CompoundTag tag, final LevelChunk chunk) {
        // no-op
    }

    @Override
    public void writeLightData(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk) {
        // no-op
    }

    @Override
    public void writeChunkAttachments(final CompoundTag tag, final RegistryAccess registryAccess, final LevelChunk chunk) {
        // deferred
    }
}
