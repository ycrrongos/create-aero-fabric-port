package dev.ryanhcode.sable.fabric.mixin.compatibility.flywheel;

import com.zurrtum.create.client.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.ryanhcode.sable.fabric.compatibility.flywheel.SableFlywheelMatrixBuffer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnvironmentStorage.class)
public class EnvironmentStorageMixin {

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/flywheel/backend/engine/CpuArena;<init>(JI)V"), index = 0)
    private long sable$overrideMatrixSize(final long elementSizeBytes) {
        return SableFlywheelMatrixBuffer.INFO_SIZE_BYTES;
    }

}
