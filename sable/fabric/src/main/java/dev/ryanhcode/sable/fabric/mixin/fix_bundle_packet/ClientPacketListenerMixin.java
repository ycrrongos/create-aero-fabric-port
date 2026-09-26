package dev.ryanhcode.sable.fabric.mixin.fix_bundle_packet;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Handles the sub-packets of bundles on the thread they arrive on, letting every sub-packet move itself to the correct thread.
 * <p>
 * In 1.21.11 packets are moved to the main thread through the {@link PacketProcessor} instead of the {@code BlockableEventLoop}.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Redirect(method = "handleBundlePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V"))
    public <T extends PacketListener> void handleOnCorrectThread(final Packet<T> packet, final T processor, final PacketProcessor packetProcessor) {
        // FIXME this sucks, implement actual fix into fabric API
    }

    /**
     * A sub-packet that has to run on the main thread schedules itself on the {@link PacketProcessor} and then throws a
     * {@link RunningOnDifferentThreadException}. Since the bundle is no longer moved to the main thread as a whole, that
     * exception must not abort the bundle, or the remaining sub-packets would never be scheduled.
     */
    @WrapOperation(method = "handleBundlePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"))
    private void sable$handleSubPacketOnCorrectThread(final Packet<?> subPacket, final PacketListener listener, final Operation<Void> original) {
        try {
            original.call(subPacket, listener);
        } catch (final RunningOnDifferentThreadException ignored) {
            // the sub-packet was scheduled onto the main thread, continue with the rest of the bundle in order
        }
    }
}
