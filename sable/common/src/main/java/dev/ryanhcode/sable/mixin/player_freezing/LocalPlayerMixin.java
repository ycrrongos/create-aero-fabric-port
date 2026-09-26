package dev.ryanhcode.sable.mixin.player_freezing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.player_freezing.PlayerFreezeExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player implements PlayerFreezeExtension {

    public LocalPlayerMixin(final Level level, final GameProfile gameProfile) {
        super(level, gameProfile);
    }

    /**
     * Keeps the local player from ticking while it's frozen to a sub-level that isn't loaded yet
     */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;hasClientLoaded()Z"))
    private boolean sable$freezeTicking(final ClientPacketListener instance, final Operation<Boolean> original) {
        this.sable$tickStopFreezing();

        final UUID uuid = this.sable$getFrozenToSubLevel();

        if (uuid != null) {
            final SubLevelContainer container = SubLevelContainer.getContainer(this.level());
            assert container != null;
            final ClientSubLevel subLevel = (ClientSubLevel) container.getSubLevel(uuid);

            if (subLevel == null || !subLevel.isFinalized()) {
                return false;
            }

            this.sable$teleport();
            this.sable$freezeTo(null, null);
        }

        return original.call(instance);
    }
}
