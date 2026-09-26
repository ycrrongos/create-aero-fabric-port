package dev.ryanhcode.sable.mixin.debug_render;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinhelpers.debug.OrientedBoxGizmo;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Debug highlights of blocks inside sub-levels are drawn where the block actually is in the world.
 */
@Mixin(Gizmos.class)
public class GizmosMixin {

    @Inject(method = "cuboid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/gizmos/GizmoStyle;)Lnet/minecraft/gizmos/GizmoProperties;", at = @At("HEAD"), cancellable = true)
    private static void sable$blockCuboid(final BlockPos pos, final GizmoStyle style, final CallbackInfoReturnable<GizmoProperties> cir) {
        sable$subLevelCuboid(pos, 0.0F, style, cir);
    }

    @Inject(method = "cuboid(Lnet/minecraft/core/BlockPos;FLnet/minecraft/gizmos/GizmoStyle;)Lnet/minecraft/gizmos/GizmoProperties;", at = @At("HEAD"), cancellable = true)
    private static void sable$inflatedBlockCuboid(final BlockPos pos, final float inflate, final GizmoStyle style, final CallbackInfoReturnable<GizmoProperties> cir) {
        sable$subLevelCuboid(pos, inflate, style, cir);
    }

    @org.spongepowered.asm.mixin.Unique
    private static void sable$subLevelCuboid(final BlockPos pos, final float inflate, final GizmoStyle style, final CallbackInfoReturnable<GizmoProperties> cir) {
        if (Minecraft.getInstance().level == null || !Minecraft.getInstance().isSameThread()) {
            return;
        }

        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(pos);
        if (subLevel != null) {
            cir.setReturnValue(OrientedBoxGizmo.inPlot(new AABB(pos).inflate(inflate), subLevel.renderPose(), style));
        }
    }
}
