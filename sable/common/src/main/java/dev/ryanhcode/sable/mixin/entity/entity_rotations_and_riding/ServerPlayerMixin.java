package dev.ryanhcode.sable.mixin.entity.entity_rotations_and_riding;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow public ServerGamePacketListenerImpl connection;

    /**
     * The vehicle the player was riding right before its last {@link ServerPlayer#removeVehicle()}
     */
    @Unique
    private @Nullable Entity sable$dismountedVehicle = null;

    public ServerPlayerMixin(final Level level, final GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @WrapOperation(method = "startRiding", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V"))
    private void sable$adjustTeleportPacket(final ServerGamePacketListenerImpl instance, final PositionMoveRotation change, final Set<Relative> relatives, final Operation<Void> original) {
        final Entity vehicle = this.getVehicle();

        if (vehicle == null) {
            original.call(instance, change, relatives);
            return;
        }

        final SubLevel containingSubLevel = Sable.HELPER.getContaining(vehicle);

        if (containingSubLevel == null) {
            original.call(instance, change, relatives);
            return;
        }

        // move the player on the server like the teleport would, but send the client its position local to the sub-level
        // without awaiting a confirmation, so it gets placed with its own pose of the sub-level
        this.teleportSetPosition(change, relatives);
        final Vec3 pos = containingSubLevel.logicalPose().transformPositionInverse(this.position());
        this.connection.send(ClientboundPlayerPositionPacket.of(
                -1,
                new PositionMoveRotation(pos, change.deltaMovement(), change.yRot(), change.xRot()),
                relatives
        ));
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void sable$rememberDismountedVehicle(final CallbackInfo ci) {
        this.sable$dismountedVehicle = this.getVehicle();
    }

    /**
     * Players dismounting a vehicle inside a sub-level work out their dismount position on the client, local to their
     * own pose of the sub-level. The server only moves them there, without re-syncing them with an absolute teleport
     * computed from the server-side pose of the sub-level.
     */
    @Override
    public void dismountTo(final double x, final double y, final double z) {
        final Entity vehicle = this.sable$dismountedVehicle;
        this.sable$dismountedVehicle = null;

        if (vehicle != null && Sable.HELPER.getContaining(vehicle) != null) {
            this.setPos(x, y, z);
            return;
        }

        super.dismountTo(x, y, z);
    }
}
