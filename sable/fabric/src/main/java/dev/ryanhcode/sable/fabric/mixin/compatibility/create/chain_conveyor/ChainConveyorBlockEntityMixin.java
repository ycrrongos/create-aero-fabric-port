package dev.ryanhcode.sable.fabric.mixin.compatibility.create.chain_conveyor;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ChainConveyorBlockEntity.class)
public abstract class ChainConveyorBlockEntityMixin {

    @Shadow
    public Map<BlockPos, ChainConveyorBlockEntity.ConnectionStats> connectionStats;

    @Shadow
    Map<BlockPos, List<ChainConveyorPackage>> travellingPackages;

    @Shadow
    private void drop(final ChainConveyorPackage box) {
    }

    @Inject(method = "removeInvalidConnections", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    public void dropInvalidPackages(final CallbackInfo ci, @Local(name = "next") final BlockPos next) {
        this.connectionStats.remove(next);

        final List<ChainConveyorPackage> packages = this.travellingPackages.remove(next);
        if (packages != null && !packages.isEmpty()) {
            for (final ChainConveyorPackage box : packages) {
                this.drop(box);
            }
        }
    }
}
