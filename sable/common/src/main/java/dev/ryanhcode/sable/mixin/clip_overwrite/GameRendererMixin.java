package dev.ryanhcode.sable.mixin.clip_overwrite;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.mixinterface.clip_overwrite.LevelPoseProviderExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Changes the block picking distance check to take into account sublevels
 * <p>
 * In 1.21.11 the picking logic ({@code pick(Entity, double, double, float)} and {@code filterHitResult}) moved from
 * {@link GameRenderer} to {@link net.minecraft.client.player.LocalPlayer}, see {@link LocalPlayerMixin}.
 * {@link GameRenderer#pick(float)} is still what runs it every frame.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"))
    private void sable$renderLevel(final GameRenderer instance, final float f, final Operation<Void> original) {
        final LevelPoseProviderExtension extension = ((LevelPoseProviderExtension) this.minecraft.level);

        extension.sable$pushPoseSupplier((subLevel) -> ((ClientSubLevel) subLevel).renderPose(f));
        original.call(instance, f);
        extension.sable$popPoseSupplier();
    }

}
