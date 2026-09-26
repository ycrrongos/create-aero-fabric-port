package dev.ryanhcode.sable.fabric.mixin.compatibility.create.frogports;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllHandle;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.infrastructure.packet.c2s.ChainPackageInteractionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Initialises the world position of packages placed onto chain conveyors, so the sub-level aware distance checks
 * of {@link ChainConveyorBlockEntityMixin} work right away.
 * <p>
 * Create Fly moved the handling of {@link ChainPackageInteractionPacket} ({@code applySettings} upstream) into a lambda
 * of {@link AllHandle#onChainPackageInteraction}.
 */
@Mixin(AllHandle.class)
public class ChainPackageInteractionPacketMixin {

    @Inject(method = "lambda$onChainPackageInteraction$16", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;addLoopingPackage(Lcom/zurrtum/create/content/kinetics/chainConveyor/ChainConveyorPackage;)Z"))
    private static void sable$initialiseLoopingWorldPosition(final CallbackInfoReturnable<Boolean> cir,
                                                             @Local(argsOnly = true) final ChainPackageInteractionPacket packet,
                                                             @Local final ChainConveyorBlockEntity be,
                                                             @Local(name = "chainConveyorPackage") final ChainConveyorPackage chainConveyorPackage) {
        chainConveyorPackage.worldPosition = be.getPackagePosition(packet.chainPosition(), null);
    }

    @Inject(method = "lambda$onChainPackageInteraction$16", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;addTravellingPackage(Lcom/zurrtum/create/content/kinetics/chainConveyor/ChainConveyorPackage;Lnet/minecraft/core/BlockPos;)Z"))
    private static void sable$initialiseTravellingWorldPosition(final CallbackInfoReturnable<Boolean> cir,
                                                                @Local(argsOnly = true) final ChainPackageInteractionPacket packet,
                                                                @Local final ChainConveyorBlockEntity be,
                                                                @Local(name = "chainConveyorPackage") final ChainConveyorPackage chainConveyorPackage) {
        chainConveyorPackage.worldPosition = be.getPackagePosition(packet.chainPosition(), packet.selectedConnection());
    }
}
