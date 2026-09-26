package dev.ryanhcode.sable.sublevel.render.dispatcher;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.index.SableTags;
import dev.ryanhcode.sable.mixin.sublevel_render.GameRendererFogAccessor;
import dev.ryanhcode.sable.render.dynamic_shade.SableDynamicDirectionalShading;
import dev.ryanhcode.sable.render.sky_light_shadow.SableSkyLightShadows;
import dev.ryanhcode.sable.render.terrain.SableTerrainShader;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderContext;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.SubLevelSectionDraws;
import dev.ryanhcode.sable.sublevel.render.vanilla.VanillaChunkedSubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.vanilla.VanillaSingleSubLevelRenderData;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class VanillaSubLevelRenderDispatcher implements SubLevelRenderDispatcher {

    private static final List<String> CHUNK_SECTION_UNIFORM = List.of("ChunkSection");

    /**
     * Sub-levels that passed the last main level culling update
     */
    private final Set<ClientSubLevel> visibleSubLevels = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean cullingUpdated;
    private int lastDrawnSections;

    public VanillaSubLevelRenderDispatcher() {
    }

    /**
     * Sets up the dynamic lighting effects of a terrain pipeline for drawing sub-levels or the world.
     *
     * @param pipeline   The terrain pipeline
     * @param onSubLevel Whether sub-levels are being drawn with dynamic lighting
     */
    public static void setupDynamicEffects(final RenderPipeline pipeline, final boolean onSubLevel) {
        SableTerrainShader.setupNormalLighting(pipeline, onSubLevel && SableDynamicDirectionalShading.isEnabled());
    }

    /**
     * Checks if this sub-level is a single block, and therefore can use simpler batched rendering
     */
    public static boolean isSingleBlock(final ClientSubLevel subLevel) {
        final BoundingBox3ic bounds = subLevel.getPlot().getBoundingBox();
        final boolean isSingle = bounds != null && bounds.minX() == bounds.maxX() && bounds.minY() == bounds.maxY() && bounds.minZ() == bounds.maxZ();
        if (!isSingle) {
            return false;
        }

        final BlockState blockState = subLevel.getLevel().getBlockState(new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ()));
        return !blockState.is(SableTags.ALWAYS_CHUNK_RENDERING);
    }

    @Override
    public void onResourceManagerReload(@NotNull final ResourceManager resourceManager) {
    }

    @Override
    public SubLevelRenderData resize(final ClientSubLevel subLevel, final SubLevelRenderData renderData) {
        if (renderData instanceof VanillaSingleSubLevelRenderData ^ isSingleBlock(subLevel)) {
            renderData.close();

            // Force-rebuild the data
            final SubLevelRenderData data = this.createRenderData(subLevel);
            if (data instanceof VanillaChunkedSubLevelRenderData) {
                data.compileSections(PrioritizeChunkUpdates.NEARBY, new RenderRegionCache(), Minecraft.getInstance().gameRenderer.getMainCamera());
            }

            return data;
        }

        if (renderData instanceof final VanillaChunkedSubLevelRenderData chunkedRenderData) {
            chunkedRenderData.resize();
            chunkedRenderData.compileSections(PrioritizeChunkUpdates.NEARBY, new RenderRegionCache(), Minecraft.getInstance().gameRenderer.getMainCamera());
        }
        return renderData;
    }

    @Override
    public SubLevelRenderData createRenderData(final ClientSubLevel subLevel) {
        if (isSingleBlock(subLevel)) {
            return new VanillaSingleSubLevelRenderData(subLevel);
        }

        final SectionRenderDispatcher sectionRenderDispatcher = Minecraft.getInstance().levelRenderer.getSectionRenderDispatcher();
        return new VanillaChunkedSubLevelRenderData(subLevel, sectionRenderDispatcher);
    }

    @Override
    public void updateCulling(final Iterable<ClientSubLevel> sublevels, final double cameraX, final double cameraY, final double cameraZ, final Frustum cullFrustum, final boolean isSpectator) {
        this.visibleSubLevels.clear();
        for (final ClientSubLevel subLevel : sublevels) {
            final BoundingBox3dc bounds = subLevel.boundingBox();
            final AABB aabb = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ()).inflate(1.0);
            if (cullFrustum.isVisible(aabb)) {
                this.visibleSubLevels.add(subLevel);
            }
        }
        this.cullingUpdated = true;
    }

    @Override
    public boolean isVisible(final ClientSubLevel subLevel) {
        return !this.cullingUpdated || this.visibleSubLevels.contains(subLevel);
    }

    @Override
    public SubLevelSectionDraws prepareSections(final Iterable<ClientSubLevel> sublevels, final SubLevelRenderContext context) {
        final GpuTextureView atlas = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        // The vanilla Globals uniform block holds the main camera position of this frame
        final Vec3 globalCamera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        final SubLevelSectionDraws draws = new SubLevelSectionDraws(context, globalCamera, atlas.getWidth(0), atlas.getHeight(0));

        for (final ClientSubLevel subLevel : sublevels) {
            if (context.mainLevel() && !this.isVisible(subLevel)) {
                continue;
            }

            subLevel.getRenderData().collectDraws(draws);
        }

        draws.upload();

        if (context.mainLevel()) {
            this.lastDrawnSections = draws.getSectionSlices().length;
        }
        return draws;
    }

    @Override
    public void renderSectionLayer(final SubLevelSectionDraws draws, final ChunkSectionLayer layer, final GpuTextureView colorTarget, final @Nullable GpuTextureView depthTarget) {
        if (draws.isEmpty()) {
            return;
        }

        boolean anyDraws = false;
        for (final SubLevelSectionDraws.Batch batch : draws.getBatches()) {
            if (!batch.draws(layer).isEmpty()) {
                anyDraws = true;
                break;
            }
        }
        if (!anyDraws) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final boolean wireframe = SharedConstants.DEBUG_HOTKEYS && minecraft.wireframe;
        final RenderPipeline pipeline = wireframe ? RenderPipelines.WIREFRAME : layer.pipeline();
        final GpuTextureView atlas = minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();

        final RenderSystem.AutoStorageIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        final GpuBuffer indexBuffer = draws.getMaxIndicesRequired() == 0 ? null : sequentialBuffer.getBuffer(draws.getMaxIndicesRequired());
        final VertexFormat.IndexType indexType = draws.getMaxIndicesRequired() == 0 ? null : sequentialBuffer.type();
        final GpuBufferSlice worldFog = RenderSystem.getShaderFog();
        final GpuBufferSlice noFog = ((GameRendererFogAccessor) minecraft.gameRenderer).sable$getFogRenderer().getBuffer(FogRenderer.FogMode.NONE);

        try (final RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Sable sub-level " + layer.label(), colorTarget, OptionalInt.empty(), depthTarget, OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.bindTexture("Sampler2", minecraft.gameRenderer.lightTexture().getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            renderPass.setPipeline(pipeline);
            renderPass.bindTexture("Sampler0", atlas, draws.getContext().sampler());

            for (final SubLevelSectionDraws.Batch batch : draws.getBatches()) {
                final List<RenderPass.Draw<GpuBufferSlice[]>> layerDraws = batch.draws(layer);
                if (layerDraws.isEmpty()) {
                    continue;
                }

                SableTerrainShader.setupSubLevel(pipeline, batch.transform(), batch.skyLightScale());
                setupDynamicEffects(pipeline, draws.getContext().normalLighting());

                if (batch.disableFog()) {
                    renderPass.setUniform("Fog", noFog);
                }

                renderPass.drawMultipleIndexed(layerDraws, indexBuffer, indexType, CHUNK_SECTION_UNIFORM, draws.getSectionSlices());

                if (batch.disableFog()) {
                    renderPass.setUniform("Fog", worldFog);
                }
            }
        } finally {
            SableTerrainShader.setupWorld(pipeline, SableSkyLightShadows.isEnabled());
            setupDynamicEffects(pipeline, false);
        }
    }

    @Override
    public void renderBlockEntities(final Iterable<ClientSubLevel> sublevels, final BlockEntityRenderer blockEntityRenderer, final double cameraX, final double cameraY, final double cameraZ, final float partialTick) {
        final Matrix4f transformation = new Matrix4f();

        for (final ClientSubLevel sublevel : sublevels) {
            final SubLevelRenderData data = sublevel.getRenderData();
            final Vec3 localCamera = sublevel.renderPose(partialTick).transformPositionInverse(new Vec3(cameraX, cameraY, cameraZ));

            data.getTransformation(cameraX, cameraY, cameraZ, transformation);
            if (data instanceof final VanillaChunkedSubLevelRenderData chunkedRenderData) {
                for (final SectionRenderDispatcher.RenderSection renderSection : chunkedRenderData.allRenderSections()) {
                    final SectionMesh mesh = renderSection.getSectionMesh();
                    final List<BlockEntity> blockEntities = mesh.getRenderableBlockEntities();
                    if (!blockEntities.isEmpty()) {
                        blockEntityRenderer.renderBlockEntities(blockEntities, sublevel, transformation, localCamera, partialTick);
                    }
                }
            } else if (data instanceof final VanillaSingleSubLevelRenderData singleRenderData) {
                final BlockEntity renderBlockEntity = singleRenderData.getRenderBlockEntity();
                if (renderBlockEntity != null) {
                    blockEntityRenderer.renderSingleBE(renderBlockEntity, sublevel, transformation, localCamera, partialTick);
                }
            }
        }
    }

    @Override
    public void addDebugInfo(final Consumer<String> consumer) {
        consumer.accept("Sable sub-level sections drawn: " + this.lastDrawnSections + ", visible sub-levels: " + this.visibleSubLevels.size());
    }

    @Override
    public void free() {
        this.visibleSubLevels.clear();
    }
}
