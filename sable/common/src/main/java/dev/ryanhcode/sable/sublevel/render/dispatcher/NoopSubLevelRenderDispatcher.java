package dev.ryanhcode.sable.sublevel.render.dispatcher;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import foundry.veil.api.client.render.CullFrustum;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * Compile-spike placeholder: sub-levels do not draw until the RenderPipeline port is done.
 */
public final class NoopSubLevelRenderDispatcher implements SubLevelRenderDispatcher {

    @Override
    public SubLevelRenderData resize(final ClientSubLevel subLevel, final SubLevelRenderData renderData) {
        if (renderData != null) {
            renderData.close();
        }
        return this.createRenderData(subLevel);
    }

    @Override
    public SubLevelRenderData createRenderData(final ClientSubLevel subLevel) {
        return new NoopSubLevelRenderData(subLevel);
    }

    @Override
    public void updateCulling(final Iterable<ClientSubLevel> sublevels, final double cameraX, final double cameraY, final double cameraZ, final CullFrustum cullFrustum, final boolean isSpectator) {
    }

    @Override
    public void renderSectionLayer(final Iterable<ClientSubLevel> sublevels, final RenderType renderType, final Object shader, final double cameraX, final double cameraY, final double cameraZ, final Matrix4f modelView, final Matrix4f projection, final float partialTicks) {
    }

    @Override
    public void renderAfterSections(final Iterable<ClientSubLevel> sublevels, final double cameraX, final double cameraY, final double cameraZ, final Matrix4f modelView, final Matrix4f projection, final float partialTicks) {
    }

    @Override
    public void renderBlockEntities(final Iterable<ClientSubLevel> sublevels, final BlockEntityRenderer blockEntityRenderer, final double cameraX, final double cameraY, final double cameraZ, final float partialTick) {
    }

    @Override
    public void addDebugInfo(final Consumer<String> consumer) {
        consumer.accept("[Sable] sub-level renderer: noop (1.21.11 spike)");
    }

    @Override
    public void onResourceManagerReload(final ResourceManager resourceManager) {
    }

    @Override
    public void free() {
    }
}
