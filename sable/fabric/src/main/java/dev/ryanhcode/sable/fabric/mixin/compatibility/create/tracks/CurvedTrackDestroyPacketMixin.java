package dev.ryanhcode.sable.fabric.mixin.compatibility.create.tracks;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Create Fly handles the curved track destroy packet in {@link AllHandle#onCurvedTrackDestroy}, where the upstream
 * {@code CurvedTrackDestroyPacket#applySettings} logic lives in the block entity configuration lambda.
 */
@Mixin(AllHandle.class)
public class CurvedTrackDestroyPacketMixin {

    @Redirect(method = "lambda$onCurvedTrackDestroy$5", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/track/TrackBlockEntity;getBlockPos()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos sable$getWorldBlockPos(final TrackBlockEntity instance) {
        return BlockPos.containing(Sable.HELPER.projectOutOfSubLevel(instance.getLevel(), instance.getBlockPos().getCenter()));
    }

}
