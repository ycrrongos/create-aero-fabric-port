package com.zurrtum.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Compile stand-in for Create's custom item renderer base. */
public abstract class CustomRenderedItemModelRenderer {
    public abstract void render(ItemStack stack, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay);

    protected abstract void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
                                   PoseStack ms, MultiBufferSource buffer, int light, int overlay);
}
