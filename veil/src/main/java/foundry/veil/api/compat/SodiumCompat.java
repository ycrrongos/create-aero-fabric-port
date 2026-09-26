package foundry.veil.api.compat;

import foundry.veil.Veil;

/**
 * Sodium compatibility queries.
 */
public final class SodiumCompat {

    private SodiumCompat() {
    }

    /**
     * @return Whether Sodium is installed
     */
    public static boolean isLoaded() {
        return Veil.SODIUM;
    }
}
