package foundry.veil.impl.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilBlockLayers;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.fabric.event.FabricVeilRenderLevelStageEvent;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Fires {@link VeilRenderLevelStageEvent} stages and flushes the fixed buffers registered for every stage.
 */
@ApiStatus.Internal
public final class VeilLevelRenderStages {

    private static final Map<VeilRenderLevelStageEvent.Stage, Set<RenderType>> STAGE_RENDER_TYPES = new EnumMap<>(VeilRenderLevelStageEvent.Stage.class);

    private static final Matrix4f FRUSTUM_MATRIX = new Matrix4f();
    private static final Matrix4f PROJECTION_MATRIX = new Matrix4f();
    @Nullable
    private static LevelRenderer levelRenderer;
    @Nullable
    private static Camera camera;
    @Nullable
    private static DeltaTracker deltaTracker;
    @Nullable
    private static Frustum frustum;
    @Nullable
    private static PoseStack poseStack;
    private static int renderTick;
    private static boolean active;

    private VeilLevelRenderStages() {
    }

    /**
     * Registers a render type that is flushed from the main buffer source at the specified stage.
     */
    public static void registerFixedBuffer(@Nullable VeilRenderLevelStageEvent.Stage stage, RenderType renderType) {
        if (stage != null) {
            STAGE_RENDER_TYPES.computeIfAbsent(stage, unused -> new ObjectArraySet<>()).add(renderType);
        }
    }

    public static void begin(LevelRenderer renderer, DeltaTracker tracker, Camera cam, Matrix4f frustumMatrix, Matrix4f projectionMatrix, int tick) {
        levelRenderer = renderer;
        deltaTracker = tracker;
        camera = cam;
        FRUSTUM_MATRIX.set(frustumMatrix);
        PROJECTION_MATRIX.set(projectionMatrix);
        renderTick = tick;
        poseStack = null;
        frustum = null;
        active = true;
    }

    public static void setFrustum(Frustum value) {
        frustum = value;
    }

    public static void setPoseStack(@Nullable PoseStack value) {
        poseStack = value;
    }

    public static void end() {
        active = false;
        levelRenderer = null;
        camera = null;
        deltaTracker = null;
        frustum = null;
        poseStack = null;
    }

    public static Matrix4f getFrustumMatrix() {
        return FRUSTUM_MATRIX;
    }

    public static Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    /**
     * Fires the specified stage.
     */
    public static void fire(VeilRenderLevelStageEvent.Stage stage) {
        if (!active || levelRenderer == null || camera == null || deltaTracker == null || frustum == null) {
            return;
        }
        ProfilerFiller profiler = Profiler.get();
        profiler.push("veil_" + stage.getName());
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack stack = poseStack != null ? poseStack : new PoseStack();
        FabricVeilRenderLevelStageEvent.EVENT.invoker().onRenderLevelStage(stage, levelRenderer, bufferSource, MatrixStack.of(stack),
                FRUSTUM_MATRIX, PROJECTION_MATRIX, renderTick, deltaTracker, camera, frustum);

        Set<RenderType> renderTypes = STAGE_RENDER_TYPES.get(stage);
        if (renderTypes != null) {
            Vec3 pos = camera.position();
            for (RenderType renderType : renderTypes) {
                if (renderType instanceof VeilRenderType veilRenderType && VeilBlockLayers.isBlockLayer(veilRenderType)) {
                    foundry.veil.impl.client.render.blocklayer.VeilBlockLayerRenderer.renderLevelLayer(veilRenderType, pos.x, pos.y, pos.z, FRUSTUM_MATRIX, PROJECTION_MATRIX);
                }
                bufferSource.endBatch(renderType);
            }
        }

        if (stage == VeilRenderLevelStageEvent.Stage.AFTER_LEVEL && !VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            VeilRenderSystem.renderer().getPostProcessingManager().runDefaultPipeline();
        }
        profiler.pop();
    }
}
