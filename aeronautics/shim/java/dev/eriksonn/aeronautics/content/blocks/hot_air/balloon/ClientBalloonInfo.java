package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon;

import net.minecraft.nbt.CompoundTag;

/** Client balloon sync stub until balloon map port compiles. */
public final class ClientBalloonInfo {
    private ClientBalloonInfo() {}

    public static void writeToNBT(final CompoundTag tag, final Object balloon) {}

    public static ClientBalloonInfo readFromNBT(final CompoundTag tag) {
        return new ClientBalloonInfo();
    }
}
