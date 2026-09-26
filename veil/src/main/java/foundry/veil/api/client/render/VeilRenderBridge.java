package foundry.veil.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Bridges vanilla render objects to their Veil counterparts.
 */
public final class VeilRenderBridge {

    private VeilRenderBridge() {
    }

    /**
     * @return A matrix stack view of the specified pose stack
     */
    public static MatrixStack create(PoseStack poseStack) {
        return MatrixStack.of(poseStack);
    }
}
