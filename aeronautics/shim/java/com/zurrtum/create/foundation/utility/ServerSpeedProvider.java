package com.zurrtum.create.foundation.utility;

/** Create Fly removed client sync helper; return unity for server-speed scaling. */
public final class ServerSpeedProvider {
    private ServerSpeedProvider() {}

    public static float get() {
        return 1f;
    }
}
