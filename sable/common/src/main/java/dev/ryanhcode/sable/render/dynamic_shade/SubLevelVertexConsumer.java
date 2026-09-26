package dev.ryanhcode.sable.render.dynamic_shade;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
/**
 * Make all shade-less things have a normal pointing straight up for dynamic shading!
 */
public class SubLevelVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private boolean verticalNormal;

    public SubLevelVertexConsumer(final VertexConsumer delegate) {
        this.delegate = delegate;
    }


    @Override
    public VertexConsumer addVertex(final float f, final float g, final float h) {
        this.delegate.addVertex(f, g, h);
        return this;
    }

    @Override
    public VertexConsumer setColor(final int i, final int j, final int k, final int l) {
        this.delegate.setColor(i, j, k, l);
        return this;
    }

    @Override
    public VertexConsumer setColor(final int color) {
        this.delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(final float f, final float g) {
        this.delegate.setUv(f, g);
        return this;
    }

    @Override
    public VertexConsumer setUv1(final int i, final int j) {
        this.delegate.setUv1(i, j);
        return this;
    }

    @Override
    public VertexConsumer setUv2(final int i, final int j) {
        this.delegate.setUv2(i, j);
        return this;
    }

    @Override
    public VertexConsumer setNormal(final float pX, final float pY, final float pZ) {
        if (this.verticalNormal) {
            this.delegate.setNormal(0f, 1f, 0f);
        } else {
            this.delegate.setNormal(pX, pY, pZ);
        }
        return this;
    }

    @Override
    public void putBulkData(final PoseStack.Pose pose, final BakedQuad bakedQuad, final float[] brightness, final float red, final float green, final float blue, final float alpha, final int[] lightmap, final int packedOverlay) {
        this.verticalNormal = !bakedQuad.shade();
        VertexConsumer.super.putBulkData(pose, bakedQuad, brightness, red, green, blue, alpha, lightmap, packedOverlay);
        this.verticalNormal = false;
    }

    @Override
    public VertexConsumer setLineWidth(final float lineWidth) {
        this.delegate.setLineWidth(lineWidth);
        return this;
    }

}