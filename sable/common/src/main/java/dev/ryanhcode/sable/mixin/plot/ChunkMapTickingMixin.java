package dev.ryanhcode.sable.mixin.plot;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Lets plot chunks be ticked by {@code ServerChunkCache#tickChunks} (inhabited time, natural spawning and random ticks).
 * <p>
 * In 1.21.1 every loaded ticking chunk of the chunk map (plot chunks included) was visited, gated by
 * {@code ServerLevel#isNaturalSpawningAllowed(ChunkPos)} (always allowed for plots, see {@link ServerLevelMixin}),
 * {@code ChunkMap#anyPlayerCloseEnoughForSpawning(ChunkPos)} (plots: tracked by a player, see {@link ChunkMapMixin}) and
 * {@code ServerLevel#shouldTickBlocksAt} (always true for plots, see {@link ServerLevelMixin}).
 * <p>
 * 1.21.11 only visits the spawn candidate and entity ticking chunks of the distance manager, which never contains plot chunks
 * since they are not loaded with tickets. The plot chunks are added to both sets here with the same conditions as before.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapTickingMixin {

    @Shadow
    @Final
    ServerLevel level;

    @Shadow
    abstract boolean anyPlayerCloseEnoughForSpawning(ChunkPos chunkPos);

    @Shadow
    public abstract DistanceManager getDistanceManager();

    @Inject(method = "collectSpawningChunks", at = @At("TAIL"))
    private void sable$collectPlotSpawningChunks(final List<LevelChunk> output, final CallbackInfo ci) {
        final List<LevelChunk> plotChunks = this.sable$collectTickingPlotChunks(false);

        if (plotChunks.isEmpty()) {
            return;
        }

        // don't visit a chunk twice if vanilla already collected it
        final Set<LevelChunk> collected = new ReferenceOpenHashSet<>(output);
        for (final LevelChunk chunk : plotChunks) {
            if (collected.add(chunk)) {
                output.add(chunk);
            }
        }
    }

    @Inject(method = "forEachBlockTickingChunk", at = @At("TAIL"))
    private void sable$forEachBlockTickingPlotChunk(final Consumer<LevelChunk> action, final CallbackInfo ci) {
        // collected first, as ticking blocks can add chunks & sub-levels
        for (final LevelChunk chunk : this.sable$collectTickingPlotChunks(true)) {
            // don't tick a chunk twice if vanilla already visited it as an entity ticking chunk
            if (ChunkLevel.isEntityTicking(this.getDistanceManager().getChunkLevel(chunk.getPos().toLong(), true))) {
                continue;
            }

            action.accept(chunk);
        }
    }

    /**
     * @param blockTicking if the chunks will have their blocks ticked
     * @return all plot chunks that should be ticked
     */
    @Unique
    private List<LevelChunk> sable$collectTickingPlotChunks(final boolean blockTicking) {
        final SubLevelContainer container = SubLevelContainer.getContainer(this.level);

        if (container == null) {
            return List.of();
        }

        final List<LevelChunk> chunks = new ObjectArrayList<>();

        for (final SubLevel subLevel : container.getAllSubLevels()) {
            for (final PlotChunkHolder holder : subLevel.getPlot().getLoadedChunks()) {
                final LevelChunk chunk = holder.getTickingChunk();
                final ChunkPos chunkPos = chunk.getPos();

                if (!this.anyPlayerCloseEnoughForSpawning(chunkPos)) {
                    continue;
                }

                if (blockTicking && !this.level.shouldTickBlocksAt(chunkPos.toLong())) {
                    continue;
                }

                chunks.add(chunk);
            }
        }

        return chunks;
    }
}
