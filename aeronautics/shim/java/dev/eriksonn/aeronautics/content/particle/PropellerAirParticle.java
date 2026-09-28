package dev.eriksonn.aeronautics.content.particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
public class PropellerAirParticle {
    public static float frictionScale = 0.95f;
    public static float lifeTime = 20f;
    public static class Factory implements ParticleProvider<Object> {
        public Factory(SpriteSet sprites) {}
    }
}
