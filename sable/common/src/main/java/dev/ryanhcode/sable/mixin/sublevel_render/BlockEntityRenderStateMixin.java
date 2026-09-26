package dev.ryanhcode.sable.mixin.sublevel_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Scales the sky light of block entities inside sub-levels like the rest of the sub-level.
 */
@Mixin(BlockEntityRenderState.class)
public class BlockEntityRenderStateMixin {

    @WrapOperation(method = "extractBase", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    private static int sable$getLightColor(final BlockAndTintGetter blockAndTintGetter, final BlockPos blockPos, final Operation<Integer> original) {
        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(blockPos);

        final int existingColor = original.call(blockAndTintGetter, blockPos);
        return subLevel != null ? subLevel.scaleLightColor(existingColor) : existingColor;
    }
}
