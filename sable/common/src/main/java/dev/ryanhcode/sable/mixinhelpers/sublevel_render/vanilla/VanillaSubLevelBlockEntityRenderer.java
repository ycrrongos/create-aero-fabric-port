package dev.ryanhcode.sable.mixinhelpers.sublevel_render.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.mixinterface.BlockEntityRenderDispatcherExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.dispatcher.SubLevelRenderDispatcher;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Extracts the render states of sub-level block entities, and submits them with the transform of their sub-level.
 */
public class VanillaSubLevelBlockEntityRenderer implements SubLevelRenderDispatcher.BlockEntityRenderer {

    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;
    private final List<Entry> entries = new ArrayList<>();
    private final Map<ClientSubLevel, CameraRenderState> cameras = new IdentityHashMap<>();

    public VanillaSubLevelBlockEntityRenderer(final BlockEntityRenderDispatcher blockEntityRenderDispatcher, final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress) {
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.destructionProgress = destructionProgress;
    }

    @Override
    public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    /**
     * @return The camera render state as seen from the plot space of the sub-level
     */
    private CameraRenderState getCamera(final ClientSubLevel subLevel, final Vec3 localCamera) {
        return this.cameras.computeIfAbsent(subLevel, key -> {
            final Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
            final CameraRenderState state = new CameraRenderState();
            state.initialized = mainCamera.isInitialized();
            state.pos = localCamera;
            state.blockPos = BlockPos.containing(localCamera);
            state.entityPos = mainCamera.entity() != null ? subLevel.renderPose().transformPositionInverse(mainCamera.entity().position()) : localCamera;
            state.orientation = new Quaternionf(subLevel.renderPose().orientation()).conjugate().mul(mainCamera.rotation());
            return state;
        });
    }

    @Override
    public void renderSingleBE(final BlockEntity blockEntity, final ClientSubLevel subLevel, final Matrix4fc transformation, final Vec3 localCamera, final float partialTick) {
        final BlockPos pos = blockEntity.getBlockPos();
        final Vector3dc rotationPoint = subLevel.renderPose().rotationPoint();

        final Matrix4f pose = new Matrix4f(transformation).translate(
                (float) (pos.getX() - rotationPoint.x()),
                (float) (pos.getY() - rotationPoint.y()),
                (float) (pos.getZ() - rotationPoint.z()));

        ModelFeatureRenderer.CrumblingOverlay crumblingOverlay = null;
        final SortedSet<BlockDestructionProgress> destructionProgresses = this.destructionProgress.get(pos.asLong());
        if (destructionProgresses != null && !destructionProgresses.isEmpty()) {
            final int progress = destructionProgresses.last().getProgress();
            if (progress >= 0) {
                final PoseStack crumblingPose = new PoseStack();
                crumblingPose.mulPose(pose);
                crumblingOverlay = new ModelFeatureRenderer.CrumblingOverlay(progress, crumblingPose.last());
            }
        }

        final BlockEntityRenderDispatcherExtension extension = (BlockEntityRenderDispatcherExtension) this.blockEntityRenderDispatcher;
        extension.sable$setCameraPosition(localCamera);
        final BlockEntityRenderState state;
        try {
            state = this.blockEntityRenderDispatcher.tryExtractRenderState(blockEntity, partialTick, crumblingOverlay);
        } finally {
            extension.sable$setCameraPosition(null);
        }

        if (state != null) {
            this.entries.add(new Entry(state, pose, this.getCamera(subLevel, localCamera)));
        }
    }

    /**
     * Submits all extracted block entities.
     *
     * @param poseStack     The pose stack, only containing the view rotation of the camera
     * @param nodeCollector The collector to submit to
     */
    public void submit(final PoseStack poseStack, final SubmitNodeCollector nodeCollector) {
        for (final Entry entry : this.entries) {
            poseStack.pushPose();
            poseStack.mulPose(entry.pose());
            this.blockEntityRenderDispatcher.submit(entry.state(), poseStack, nodeCollector, entry.camera());
            poseStack.popPose();
        }
    }

    /**
     * Clears the block entities of the previous render.
     */
    public void clear() {
        this.entries.clear();
        this.cameras.clear();
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    private record Entry(BlockEntityRenderState state, Matrix4f pose, CameraRenderState camera) {
    }
}
