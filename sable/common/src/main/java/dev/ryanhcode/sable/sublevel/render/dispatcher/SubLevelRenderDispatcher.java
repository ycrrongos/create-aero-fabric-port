package dev.ryanhcode.sable.sublevel.render.dispatcher;

import com.mojang.blaze3d.textures.GpuTextureView;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderContext;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderer;
import dev.ryanhcode.sable.sublevel.render.SubLevelSectionDraws;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.lwjgl.system.NativeResource;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Renders sub-levels into the world.
 */
@ApiStatus.Internal
public interface SubLevelRenderDispatcher extends NativeResource, ResourceManagerReloadListener {

    /**
     * @return The current sub-level renderer instance
     */
    static SubLevelRenderDispatcher get() {
        return SubLevelRenderer.getDispatcher();
    }

    /**
     * Resizes the specified render data.
     *
     * @param subLevel   The sub-level to resize
     * @param renderData The current render data
     * @return The new render data to use
     */
    SubLevelRenderData resize(final ClientSubLevel subLevel, final SubLevelRenderData renderData);

    /**
     * Creates a new render data instance for the specified sub-level.
     *
     * @param subLevel The sub-level to create render data for
     * @return A new render data instance
     */
    SubLevelRenderData createRenderData(final ClientSubLevel subLevel);

    /**
     * Rebuilds the specified sub-levels when F3+A is pressed.
     *
     * @param sublevels The sub-levels to rebuild
     */
    default void rebuild(final Iterable<ClientSubLevel> sublevels) {
        for (final ClientSubLevel sublevel : sublevels) {
            sublevel.getRenderData().rebuild();
        }
    }

    /**
     * Updates the current culling state for all sub-levels.
     *
     * @param sublevels   The sub-levels to update
     * @param cameraX     The x position of the camera
     * @param cameraY     The y position of the camera
     * @param cameraZ     The z position of the camera
     * @param cullFrustum The current frustum used for culling
     * @param isSpectator Whether the player is in spectator mode
     */
    void updateCulling(final Iterable<ClientSubLevel> sublevels, final double cameraX, final double cameraY, final double cameraZ, final Frustum cullFrustum, boolean isSpectator);

    /**
     * @return Whether the sub-level passed the last culling update of the main level
     */
    boolean isVisible(final ClientSubLevel subLevel);

    /**
     * Collects the section draws of the specified sub-levels.
     *
     * @param sublevels The sub-levels to render
     * @param context   How the sub-levels are rendered
     * @return The draws, to pass to {@link #renderSectionLayer}
     */
    SubLevelSectionDraws prepareSections(final Iterable<ClientSubLevel> sublevels, final SubLevelRenderContext context);

    /**
     * Draws a chunk layer of prepared sub-levels.
     *
     * @param draws       The prepared draws
     * @param layer       The chunk layer to draw
     * @param colorTarget The color texture to draw into
     * @param depthTarget The depth texture to draw into
     */
    void renderSectionLayer(final SubLevelSectionDraws draws, final ChunkSectionLayer layer, final GpuTextureView colorTarget, final @Nullable GpuTextureView depthTarget);

    /**
     * Collects the block entities of all sub-levels.
     *
     * @param sublevels           The sub-levels to render
     * @param blockEntityRenderer The renderer to hand the block entities to
     * @param cameraX             The x position of the camera
     * @param cameraY             The y position of the camera
     * @param cameraZ             The z position of the camera
     * @param partialTick         The partial tick
     */
    void renderBlockEntities(final Iterable<ClientSubLevel> sublevels, final BlockEntityRenderer blockEntityRenderer, final double cameraX, double cameraY, double cameraZ, final float partialTick);

    void addDebugInfo(final Consumer<String> consumer);

    default void preRenderChunks(final Camera camera) {
    }

    interface BlockEntityRenderer {

        /**
         * Renders block entities of a sub-level.
         *
         * @param blockEntities  The block entities, in plot space
         * @param subLevel       The sub-level containing the block entities
         * @param transformation Transforms positions relative to the rotation point of the sub-level into camera relative world space
         * @param localCamera    The camera position in plot space
         * @param partialTick    The partial tick
         */
        default void renderBlockEntities(final Collection<BlockEntity> blockEntities, final ClientSubLevel subLevel, final Matrix4fc transformation, final Vec3 localCamera, final float partialTick) {
            for (final BlockEntity blockEntity : blockEntities) {
                this.renderSingleBE(blockEntity, subLevel, transformation, localCamera, partialTick);
            }
        }

        void renderSingleBE(final BlockEntity blockEntity, final ClientSubLevel subLevel, final Matrix4fc transformation, final Vec3 localCamera, final float partialTick);

        BlockEntityRenderDispatcher getBlockEntityRenderDispatcher();
    }
}
