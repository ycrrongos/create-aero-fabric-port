package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Client balloon sync stub until balloon map port compiles. */
public final class ClientBalloonInfo {
    private ClientBalloonInfo() {}

    public static void writeToNBT(final ValueOutput tag, final Object balloon) {}

    public static ClientBalloonInfo readFromNBT(final ValueInput tag) {
        return new ClientBalloonInfo();
    }
}
