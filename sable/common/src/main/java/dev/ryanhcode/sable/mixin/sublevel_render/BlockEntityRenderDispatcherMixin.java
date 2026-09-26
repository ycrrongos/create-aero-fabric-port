package dev.ryanhcode.sable.mixin.sublevel_render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ryanhcode.sable.mixinterface.BlockEntityRenderDispatcherExtension;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Block entities of sub-levels are extracted with the camera moved into the plot space of their sub-level.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin implements BlockEntityRenderDispatcherExtension {

    @Unique
    private @Nullable Vec3 sable$cameraPos;

    @ModifyExpressionValue(method = "tryExtractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;cameraPos:Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 sable$useSubLevelCamera(final Vec3 cameraPos) {
        return this.sable$cameraPos != null ? this.sable$cameraPos : cameraPos;
    }

    @Override
    public void sable$setCameraPosition(@Nullable final Vec3 pos) {
        this.sable$cameraPos = pos;
    }
}
