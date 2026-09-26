package dev.ryanhcode.sable.fabric.mixin.compatibility.create.frogports;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorShape;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChainConveyorShape.class)
public interface ChainConveyorShapeAccessor {

    @Invoker
    void invokeDrawOutline(BlockPos anchor, PoseStack ms, VertexConsumer vb);

}
