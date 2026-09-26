package dev.ryanhcode.sable.mixin.world_border;

import dev.ryanhcode.sable.mixinterface.world_border.WorldBorderExtension;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives the world border of client levels access to their level, so that sub-level plots are always considered in bounds.
 * <p>
 * In 1.21.11 the world border is no longer a field of {@code Level}; client levels own their own instance.
 * The server level counterpart is {@link LevelMixin}.
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Shadow @Final private WorldBorder worldBorder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sable$initializeWorldBorder(final CallbackInfo ci) {
        ((WorldBorderExtension) this.worldBorder).sable$setLevel((ClientLevel) (Object) this);
    }

}
