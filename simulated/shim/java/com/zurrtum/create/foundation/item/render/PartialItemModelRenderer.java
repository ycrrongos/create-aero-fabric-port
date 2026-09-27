package com.zurrtum.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

/** Compile stand-in for Create's custom item model renderer helper. */
public class PartialItemModelRenderer {
    public static PartialItemModelRenderer of(CustomRenderedItemModel model, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        return new PartialItemModelRenderer();
    }

    public PartialItemModelRenderer render(Object model, RenderType type) {
        return this;
    }

    public PartialItemModelRenderer render(Object model, RenderType type, int light) {
        return this;
    }

    public PartialItemModelRenderer renderSolid(Object model, int light) {
        return this;
    }

    public PartialItemModelRenderer renderGlowing(Object model, RenderType type, int light) {
        return this;
    }
}
