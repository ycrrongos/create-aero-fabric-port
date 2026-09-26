package dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.packet_mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface PacketActuallyInSubLevelExtension {
    void sable$setActuallyInSubLevel(boolean actuallyInSubLevel);

    boolean sable$isActuallyInSubLevel();

    /**
     * Wraps a packet codec to additionally network the "actually in sub-level" flag after the vanilla contents
     */
    static <T> StreamCodec<FriendlyByteBuf, T> wrapCodec(final StreamCodec<FriendlyByteBuf, T> original) {
        return StreamCodec.of((buf, packet) -> {
            original.encode(buf, packet);
            buf.writeBoolean(((PacketActuallyInSubLevelExtension) packet).sable$isActuallyInSubLevel());
        }, buf -> {
            final T packet = original.decode(buf);
            ((PacketActuallyInSubLevelExtension) packet).sable$setActuallyInSubLevel(buf.readBoolean());
            return packet;
        });
    }
}
