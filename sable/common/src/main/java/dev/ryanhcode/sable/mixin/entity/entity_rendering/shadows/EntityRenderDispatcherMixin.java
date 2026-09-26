package dev.ryanhcode.sable.mixin.entity.entity_rendering.shadows;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.mixinhelpers.entity.entity_rendering.shadows.SubLevelEntityShadowRenderer;
import dev.ryanhcode.sable.mixinterface.entity.entity_rendering.EntityRenderStateExtension;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Submits the shadows entities cast onto sub-levels, next to their vanilla shadow.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Unique
    private static final RenderType SABLE$SHADOW_RENDER_TYPE = RenderTypes.entityShadow(Identifier.withDefaultNamespace("textures/misc/shadow.png"));

    @Inject(method = "submit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private <S extends EntityRenderState> void sable$submitShadowsOnSubLevels(final S renderState, final CameraRenderState cameraRenderState, final double camX, final double camY, final double camZ,
                                                                             final PoseStack poseStack, final SubmitNodeCollector nodeCollector, final CallbackInfo ci) {
        final float[] vertices = ((EntityRenderStateExtension) renderState).sable$getSubLevelShadow();
        if (vertices == null) {
            return;
        }

        nodeCollector.submitCustomGeometry(poseStack, SABLE$SHADOW_RENDER_TYPE, (pose, consumer) -> SubLevelEntityShadowRenderer.emit(vertices, pose, consumer));
    }
}
