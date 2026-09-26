package dev.ryanhcode.sable.sublevel.render.vanilla;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.sublevel_render.vanilla.RenderSectionExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderContext;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.SubLevelSectionDraws;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionContainer;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.Collection;

/**
 * A renderer and view area for a {@link dev.ryanhcode.sable.sublevel.SubLevel}.
 * <p>
 * The sections of the plot are compiled by the vanilla section render dispatcher, and drawn with the vanilla terrain
 * pipelines by {@link dev.ryanhcode.sable.sublevel.render.dispatcher.VanillaSubLevelRenderDispatcher}.
 */
public class VanillaChunkedSubLevelRenderData implements SubLevelRenderData {

    /**
     * The origin(minimum) of the render section grid in blocks
     */
    private final Vector3i origin = new Vector3i();
    /**
     * The origin(minimum) of the render section grid in sections
     */
    private final Vector3i chunkOrigin = new Vector3i();
    /**
     * The sub-level this renderer is for
     */
    private final ClientSubLevel subLevel;
    /**
     * The size of the render section grid
     */
    private final Vector3i size = new Vector3i();
    /**
     * All render sections this renderer stores
     */
    private final ObjectList<SectionRenderDispatcher.RenderSection> allRenderSections = new ObjectArrayList<>();
    /**
     * All dirty render sections this renderer stores
     */
    private final ObjectList<SectionRenderDispatcher.RenderSection> dirtyRenderSections = new ObjectArrayList<>();
    /**
     * The chunk columns registered for camera lookups
     */
    private final ObjectList<ChunkColumn> registeredColumns = new ObjectArrayList<>();
    /**
     * The grid of render sections
     */
    private SectionRenderDispatcher.RenderSection[] renderSections = null;
    /**
     * The section render dispatcher to build sections through
     */
    private final SectionRenderDispatcher sectionRenderDispatcher;
    /**
     * Reused to check whether translucent sections need to be sorted again
     */
    private final TranslucencyPointOfView pointOfView = new TranslucencyPointOfView();
    /**
     * The camera position in plot space, updated every frame and read by worker threads
     */
    private volatile @Nullable Vec3 localCamera;

    /**
     * Creates a new renderer for the given sub-level
     *
     * @param subLevel the sub-level to render
     */
    public VanillaChunkedSubLevelRenderData(final ClientSubLevel subLevel, final SectionRenderDispatcher sectionRenderDispatcher) {
        this.subLevel = subLevel;
        this.sectionRenderDispatcher = sectionRenderDispatcher;
        this.resize();
    }

    /**
     * Gets a section in global section coordinates
     *
     * @param sections the section array
     * @param size     the dimensions of the section grid
     * @param origin   the origin of the section grid
     * @param x        the global x coordinate
     * @param y        the global y coordinate
     * @param z        the global z coordinate
     * @return the section if it exists
     */
    private static SectionRenderDispatcher.RenderSection getSection(final SectionRenderDispatcher.RenderSection[] sections, final Vector3i size, final Vector3i origin, final int x, final int y, final int z) {
        if (sections == null) {
            return null;
        }

        final int relX = (x - origin.x());
        final int relY = (y - origin.y());
        final int relZ = (z - origin.z());

        if (relX < 0 || relY < 0 || relZ < 0) {
            return null;
        }

        if (relX >= size.x() || relY >= size.y() || relZ >= size.z()) {
            return null;
        }

        return sections[relX + relY * size.x() + relZ * size.x() * size.y()];
    }

    /**
     * Gets an index in the render section grid from a global position
     */
    private int getIndex(final int x, final int y, final int z) {
        return (x - this.chunkOrigin.x()) + (y - this.chunkOrigin.y()) * this.size.x() + (z - this.chunkOrigin.z()) * this.size.x() * this.size.y();
    }

    /**
     * Checks if a global section coordinate is in bounds
     */
    private boolean inBounds(final int x, final int y, final int z) {
        final int localX = x - this.chunkOrigin.x();
        final int localY = y - this.chunkOrigin.y();
        final int localZ = z - this.chunkOrigin.z();
        return localX >= 0 && localY >= 0 && localZ >= 0 &&
                localX < this.size.x() && localY < this.size.y() && localZ < this.size.z();

    }

    public void resize() {
        final SectionRenderDispatcher.RenderSection[] oldRenderSections = this.renderSections;
        final Collection<SectionRenderDispatcher.RenderSection> oldRenderSectionsList = new ObjectArrayList<>(this.allRenderSections);

        this.renderSections = null;
        this.allRenderSections.clear();
        this.dirtyRenderSections.clear();
        this.unregisterColumns();

        final BoundingBox3ic bounds = this.subLevel.getPlot().getBoundingBox();

        if (bounds != null && !bounds.equals(BoundingBox3i.EMPTY) && bounds.volume() > 0.0) {
            final Vector3i minChunkPos = new Vector3i(bounds.minX() >> 4, bounds.minY() >> 4, bounds.minZ() >> 4);
            final Vector3i maxChunkPos = new Vector3i(bounds.maxX() >> 4, bounds.maxY() >> 4, bounds.maxZ() >> 4);

            final Vector3i oldSize = new Vector3i(this.size);
            final Vector3i oldOrigin = new Vector3i(this.chunkOrigin);

            this.size.set(maxChunkPos.x() - minChunkPos.x() + 1, maxChunkPos.y() - minChunkPos.y() + 1, maxChunkPos.z() - minChunkPos.z() + 1);
            this.chunkOrigin.set(minChunkPos);
            this.origin.set(minChunkPos.x() << 4, minChunkPos.y() << 4, minChunkPos.z() << 4);

            this.renderSections = new SectionRenderDispatcher.RenderSection[this.size.x() * this.size.y() * this.size.z()];

            for (int x = minChunkPos.x(); x <= maxChunkPos.x(); x++) {
                for (int z = minChunkPos.z(); z <= maxChunkPos.z(); z++) {
                    SubLevelSectionCameras.register(x, z, this);
                    this.registeredColumns.add(new ChunkColumn(x, z));
                }
            }

            for (int x = minChunkPos.x(); x <= maxChunkPos.x(); x++) {
                for (int y = minChunkPos.y(); y <= maxChunkPos.y(); y++) {
                    for (int z = minChunkPos.z(); z <= maxChunkPos.z(); z++) {
                        final SectionRenderDispatcher.RenderSection oldSection = getSection(oldRenderSections, oldSize, oldOrigin, x, y, z);
                        final SectionRenderDispatcher.RenderSection newSection;

                        if (oldSection != null) {
                            newSection = oldSection;
                        } else {
                            newSection = this.sectionRenderDispatcher.new RenderSection(-1, SectionPos.asLong(x, y, z));
                            ((RenderSectionExtension) newSection).sable$addDirtyListener(this.dirtyRenderSections::add);
                        }

                        if (newSection.isDirty()) {
                            this.dirtyRenderSections.add(newSection);
                        }
                        this.renderSections[this.getIndex(x, y, z)] = newSection;
                        this.allRenderSections.add(newSection);
                    }
                }
            }

            // free old chunks
            if (oldRenderSections != null) {
                for (final SectionRenderDispatcher.RenderSection oldSection : oldRenderSectionsList) {
                    // if not in bounds
                    final long oldSectionNode = oldSection.getSectionNode();
                    final int oldX = SectionPos.x(oldSectionNode);
                    final int oldY = SectionPos.y(oldSectionNode);
                    final int oldZ = SectionPos.z(oldSectionNode);
                    if (oldX < minChunkPos.x() || oldX > maxChunkPos.x() ||
                            oldY < minChunkPos.y() || oldY > maxChunkPos.y() ||
                            oldZ < minChunkPos.z() || oldZ > maxChunkPos.z()) {
                        oldSection.reset();
                    }
                }
            }
        } else if (oldRenderSections != null) {
            for (final SectionRenderDispatcher.RenderSection oldSection : oldRenderSectionsList) {
                oldSection.reset();
            }
        }
    }

    private void unregisterColumns() {
        for (final ChunkColumn column : this.registeredColumns) {
            SubLevelSectionCameras.unregister(column.x(), column.z(), this);
        }
        this.registeredColumns.clear();
    }

    @Override
    public void rebuild() {
        for (final SectionRenderDispatcher.RenderSection renderSection : this.allRenderSections) {
            renderSection.setDirty(true);
        }
    }

    @Override
    public void compileSections(final PrioritizeChunkUpdates chunkUpdates, final RenderRegionCache renderRegionCache, final Camera camera) {
        if (this.dirtyRenderSections.isEmpty()) {
            return;
        }

        final ProfilerFiller profiler = Profiler.get();
        final Vector3d cameraPos = JOMLConversion.atCenterOf(camera.blockPosition()).sub(8, 8, 8);
        this.subLevel.logicalPose().transformPositionInverse(cameraPos);

        for (final SectionRenderDispatcher.RenderSection renderSection : this.dirtyRenderSections) {
            ((RenderSectionExtension) renderSection).sable$setListening(false);

            boolean buildSync = false;
            if (chunkUpdates == PrioritizeChunkUpdates.NEARBY) {
                final BlockPos origin = renderSection.getRenderOrigin();
                buildSync = cameraPos.distanceSquared(origin.getX(), origin.getY(), origin.getZ()) < 768.0 || renderSection.isDirtyFromPlayer();
            } else if (chunkUpdates == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                buildSync = renderSection.isDirtyFromPlayer();
            }

            if (buildSync) {
                profiler.push("sublevel_build_near_sync");
                this.sectionRenderDispatcher.rebuildSectionSync(renderSection, renderRegionCache);
                profiler.pop();
            } else {
                profiler.push("sublevel_schedule_async_compile");
                renderSection.rebuildSectionAsync(renderRegionCache);
                profiler.pop();
            }

            renderSection.setNotDirty();
            ((RenderSectionExtension) renderSection).sable$setListening(true);
        }
        this.dirtyRenderSections.clear();
    }

    @Override
    public int getVisibleSectionCount() {
        return this.allRenderSections.size();
    }

    @Override
    public ClientSubLevel getSubLevel() {
        return this.subLevel;
    }

    @Override
    public boolean isSectionCompiled(final int x, final int y, final int z) {
        if (this.renderSections == null) {
            return false;
        }

        if (!this.inBounds(x, y, z)) {
            return true;
        }

        final int index = this.getIndex(x, y, z);
        return index >= 0 && index < this.renderSections.length && this.renderSections[index].sectionMesh.get() != CompiledSectionMesh.UNCOMPILED;
    }

    @Override
    public void setDirty(final int x, final int y, final int z, final boolean playerChanged) {
        if (this.renderSections == null) {
            return;
        }

        if (!this.inBounds(x, y, z)) {
            return;
        }

        final int index = this.getIndex(x, y, z);
        if (index >= 0 && index < this.renderSections.length) {
            this.renderSections[index].setDirty(playerChanged);
        }
    }

    /**
     * @return all render sections this renderer stores
     */
    public ObjectList<SectionRenderDispatcher.RenderSection> allRenderSections() {
        return this.allRenderSections;
    }

    /**
     * @return The camera position in the plot space of this sub-level, as of the last rendered frame
     */
    public @Nullable Vec3 getLocalCamera() {
        return this.localCamera;
    }

    /**
     * @return Whether the camera is inside a water occlusion region of this sub-level, in which case fog is not applied to it
     */
    private boolean isCameraInOwnOcclusionRegion(final SubLevelRenderContext context) {
        final WaterOcclusionContainer<?> container = WaterOcclusionContainer.getContainer(this.subLevel.getLevel());
        if (container == null) {
            return false;
        }

        final WaterOcclusionRegion occludingRegion = container.getOccludingRegion(new Vec3(context.cameraX(), context.cameraY(), context.cameraZ()));
        return occludingRegion != null && Sable.HELPER.getContaining(this.subLevel.getLevel(), occludingRegion.getVolume().getMinBlockPos()) == this.subLevel;
    }

    @Override
    public void collectDraws(final SubLevelSectionDraws draws) {
        final SubLevelRenderContext context = draws.getContext();
        final Pose3dc renderPose = this.subLevel.renderPose(context.partialTicks());
        final Vec3 localCamera = renderPose.transformPositionInverse(new Vec3(context.cameraX(), context.cameraY(), context.cameraZ()));

        if (context.mainLevel()) {
            this.localCamera = localCamera;
        }

        final boolean disableFog = context.mainLevel() && this.isCameraInOwnOcclusionRegion(context);
        final SubLevelSectionDraws.Batch batch = draws.begin(this.subLevel, renderPose, this.origin.x(), this.origin.y(), this.origin.z(),
                this.subLevel.getLatestSkyLightScale() / 15.0F, disableFog);

        for (final SectionRenderDispatcher.RenderSection renderSection : this.allRenderSections) {
            final SectionMesh mesh = renderSection.getSectionMesh();
            if (!(mesh instanceof final CompiledSectionMesh compiledMesh) || !compiledMesh.hasRenderableLayers()) {
                continue;
            }

            final BlockPos sectionOrigin = renderSection.getRenderOrigin();
            final double sortDistance = localCamera.distanceToSqr(sectionOrigin.getX() + 8.0, sectionOrigin.getY() + 8.0, sectionOrigin.getZ() + 8.0);
            draws.addSection(batch, sectionOrigin.getX(), sectionOrigin.getY(), sectionOrigin.getZ(), sortDistance, compiledMesh::getBuffers);

            if (context.mainLevel()) {
                this.scheduleResort(renderSection, compiledMesh, localCamera);
            }
        }
    }

    /**
     * Sorts the translucent geometry of a section again when the camera moved relative to it, like vanilla does for
     * world sections.
     */
    private void scheduleResort(final SectionRenderDispatcher.RenderSection section, final CompiledSectionMesh mesh, final Vec3 localCamera) {
        if (!mesh.hasTranslucentGeometry() || section.transparencyResortingScheduled()) {
            return;
        }

        this.pointOfView.set(localCamera, section.getSectionNode());
        if (mesh.isDifferentPointOfView(this.pointOfView)) {
            section.resortTransparency(this.sectionRenderDispatcher);
        }
    }

    @Override
    public void close() {
        for (final SectionRenderDispatcher.RenderSection section : this.allRenderSections) {
            section.reset();
        }
        this.allRenderSections.clear();
        this.dirtyRenderSections.clear();
        this.unregisterColumns();
        this.renderSections = null;
    }

    public SectionRenderDispatcher.RenderSection getRenderSection(final SectionPos sectionPos) {
        if (this.renderSections == null || !this.inBounds(sectionPos.getX(), sectionPos.getY(), sectionPos.getZ())) {
            return null;
        }

        return this.renderSections[this.getIndex(sectionPos.getX(), sectionPos.getY(), sectionPos.getZ())];
    }

    private record ChunkColumn(int x, int z) {
    }
}
