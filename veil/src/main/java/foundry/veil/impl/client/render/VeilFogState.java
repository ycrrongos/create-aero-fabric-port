package foundry.veil.impl.client.render;

import org.jetbrains.annotations.ApiStatus;

/**
 * The most recent fog parameters written by the vanilla fog renderer, captured so classic (non uniform block) shaders can
 * still receive <code>FogStart</code>, <code>FogEnd</code> and <code>FogColor</code>.
 */
@ApiStatus.Internal
public record VeilFogState(float red, float green, float blue, float alpha,
                           float environmentalStart, float environmentalEnd,
                           float renderDistanceStart, float renderDistanceEnd,
                           float skyEnd, float cloudEnd) {

    private static VeilFogState current = new VeilFogState(0.0F, 0.0F, 0.0F, 0.0F, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    public static VeilFogState get() {
        return current;
    }

    public static void set(VeilFogState state) {
        current = state;
    }

    /**
     * Classic shaders only support a single linear fog. The closest of the two 1.21.11 fogs is used, which matches the
     * behavior of 1.21.1 where environmental fog (water, lava, blindness...) replaced render distance fog.
     */
    public float legacyStart() {
        return this.environmentalEnd < this.renderDistanceEnd ? this.environmentalStart : this.renderDistanceStart;
    }

    public float legacyEnd() {
        return Math.min(this.environmentalEnd, this.renderDistanceEnd);
    }

    /**
     * @return <code>0</code> for spherical fog (environmental) and <code>1</code> for cylindrical fog (render distance)
     */
    public int legacyShape() {
        return this.environmentalEnd < this.renderDistanceEnd ? 0 : 1;
    }
}
