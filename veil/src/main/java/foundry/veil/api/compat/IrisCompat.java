package foundry.veil.api.compat;

import foundry.veil.Veil;

/**
 * Iris compatibility queries.
 */
public final class IrisCompat {

    private IrisCompat() {
    }

    /**
     * @return Whether Iris is installed
     */
    public static boolean isLoaded() {
        return Veil.IRIS;
    }
}
