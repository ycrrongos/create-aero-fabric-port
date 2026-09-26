package dev.ryanhcode.sable.fabric.mixin.compatibility.create.render_fixes;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.util.SublevelRenderOffsetHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.zurrtum.create.client.catnip.ghostblock.GhostBlockRenderer$TransparentGhostBlockRenderer")
public abstract class GhostBlockValueBoxMixin {

    // Additional pose stack has already been pushed by here
    // Create Fly passes the camera position the ghost block is translated against, use it to recover the block position
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", ordinal = 0))
    public void sable$translate(final PoseStack ms, final double pX, final double pY, final double pZ, @Local(argsOnly = true) final Vec3 camera) {
        final Vec3 center = new Vec3(pX + camera.x, pY + camera.y, pZ + camera.z);
        SublevelRenderOffsetHelper.posePlotToProjected(Sable.HELPER.getContainingClient(center), ms);

        final Vec3 translation = SublevelRenderOffsetHelper.translation(center);

        ms.translate(pX - translation.x, pY - translation.y, pZ - translation.z);
    }
}
