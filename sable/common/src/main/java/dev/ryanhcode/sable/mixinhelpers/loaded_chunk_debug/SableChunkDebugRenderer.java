package dev.ryanhcode.sable.mixinhelpers.loaded_chunk_debug;

import dev.ryanhcode.sable.mixinterface.loaded_chunk_debug.DebugChunkProviderAttachments;
import dev.ryanhcode.sable.mixinterface.loaded_chunk_debug.DebugLevelChunkExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

/**
 * Replaces the chunk border debug view with an overview of the loaded client chunks, highlighting recently updated ones.
 */
public class SableChunkDebugRenderer {

    private static void rect(final double x, final double y, final double z, final int color) {
        final Vec3 a = new Vec3(x, y, z);
        final Vec3 b = new Vec3(x + 16, y, z);
        final Vec3 c = new Vec3(x + 16, y, z + 16);
        final Vec3 d = new Vec3(x, y, z + 16);
        Gizmos.line(a, b, color, 1.0F);
        Gizmos.line(b, c, color, 1.0F);
        Gizmos.line(c, d, color, 1.0F);
        Gizmos.line(d, a, color, 1.0F);
    }

    public static void emitGizmos(final double camX, final double camY, final double camZ) {
        final long time = System.currentTimeMillis();

        final Minecraft minecraft = Minecraft.getInstance();
        final Entity entity = minecraft.gameRenderer.getMainCamera().entity();

        final ClientLevel level = minecraft.level;
        final int minBuildHeight = level.getMinY();
        final int maxBuildHeight = level.getMaxY() + 1;

        final DebugChunkProviderAttachments attachments = (DebugChunkProviderAttachments) level.getChunkSource();
        for (final LevelChunk chunk : attachments.sable$loadedChunks()) {
            final ChunkPos pos = chunk.getPos();
            final float diff = Mth.clamp(time - ((DebugLevelChunkExtension) chunk).sable$getLastUpdate(), 0, 1000) / 1000.0F;
            final int color = ARGB.colorFromFloat(1.0F, 1.0F - diff, diff, 0.0F);

            double y = minBuildHeight;
            if (camY > minBuildHeight) {
                y += (10 * ((1 - diff) / 100));
            } else {
                y -= (10 * ((1 - diff) / 100));
            }
            double y1 = maxBuildHeight;
            if (camY < maxBuildHeight) {
                y1 -= (10 * ((1 - diff) / 100));
            } else {
                y1 += (10 * ((1 - diff) / 100));
            }

            rect(pos.getMinBlockX(), y, pos.getMinBlockZ(), color);
            rect(pos.getMinBlockX(), y1, pos.getMinBlockZ(), color);
        }

        final ChunkPos ckPos = entity.chunkPosition();
        final double x = ckPos.x * 16;
        final double z = ckPos.z * 16;
        final int yellow = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 0.0F);

        for (int xO = 0; xO < 2; xO++) {
            for (int zO = 0; zO < 2; zO++) {
                Gizmos.line(new Vec3(x + xO * 16, minBuildHeight, z + zO * 16), new Vec3(x + xO * 16, maxBuildHeight, z + zO * 16), yellow, 1.0F);
            }
        }

        final int blue = ARGB.colorFromFloat(1.0F, 0.0F, 0.0F, 1.0F);
        final int start = (minBuildHeight / 16) * 16;
        for (int yO = start; yO <= maxBuildHeight + 1; yO += 16) {
            rect(x, yO, z, blue);
        }
    }
}
