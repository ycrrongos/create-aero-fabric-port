package dev.simulated_team.simulated.index;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

/**
 * 1.21.11 builds render types from a RenderPipeline plus RenderSetup.
 * These use the closest vanilla pipelines so callers compile. Veil shader variants are not wired yet.
 */
public final class SimRenderTypes {
    private SimRenderTypes() {}

    private static RenderType textured(String name, RenderPipeline pipeline, Identifier texture, boolean lightmap, boolean overlay, boolean sort) {
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(pipeline)
                .withTexture("Sampler0", texture)
                .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE);
        if (lightmap) {
            builder.useLightmap();
        }
        if (overlay) {
            builder.useOverlay();
        }
        if (sort) {
            builder.sortOnUpload();
        }
        return RenderType.create(Simulated.MOD_ID + ":" + name, builder.createRenderSetup());
    }

    private static final RenderType STAFF_OVERLAY = RenderType.create(
            Simulated.MOD_ID + ":staff_overlay",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                    .withTexture("Sampler0", Simulated.path("textures/misc/white.png"))
                    .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                    .sortOnUpload()
                    .createRenderSetup());

    private static final RenderType LASER = textured("laser", RenderPipelines.ENTITY_TRANSLUCENT, Simulated.path("textures/misc/laser.png"), false, false, true);
    private static final RenderType LENS = textured("laser_pointer_lens", RenderPipelines.CUTOUT_BLOCK, Simulated.path("textures/block/laser_pointer_lens.png"), true, false, true);
    private static final RenderType LOCK = textured("lock", RenderPipelines.ENTITY_TRANSLUCENT, Simulated.path("textures/gui/lock.png"), true, false, false);
    private static final RenderType ROPE = textured("rope", RenderPipelines.ENTITY_CUTOUT, Simulated.path("textures/block/rope_particle.png"), true, false, false);

    private static final Function<Identifier, RenderType> SPRING = Util.memoize(
            (Identifier texture) -> textured("spring", RenderPipelines.ENTITY_CUTOUT, texture, true, true, false));

    public static RenderType staffOverlay() {
        return STAFF_OVERLAY;
    }

    public static RenderType laser() {
        return LASER;
    }

    public static RenderType lens() {
        return LENS;
    }

    public static RenderType lock() {
        return LOCK;
    }

    public static RenderType rope() {
        return ROPE;
    }

    public static RenderType itemGlowingSolid(boolean shadersActive) {
        return Sheets.solidBlockSheet();
    }

    public static RenderType itemGlowingTranslucent(boolean shadersActive) {
        return Sheets.translucentItemSheet();
    }

    public static RenderType spring(Identifier texture) {
        return SPRING.apply(texture);
    }
}
