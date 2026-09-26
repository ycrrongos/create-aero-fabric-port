package foundry.veil.api.client.render;

/**
 * Tracks whether the level is currently rendered from a secondary perspective (for example into a shadow map), so that
 * effects that only make sense for the player camera can skip themselves.
 */
public final class VeilLevelPerspectiveRenderer {

    private static int depth;

    private VeilLevelPerspectiveRenderer() {
    }

    public static boolean isRenderingPerspective() {
        return depth > 0;
    }

    /**
     * Marks the start of a secondary perspective render. Must be paired with {@link #endPerspective()}.
     */
    public static void beginPerspective() {
        depth++;
    }

    public static void endPerspective() {
        if (depth > 0) {
            depth--;
        }
    }
}
