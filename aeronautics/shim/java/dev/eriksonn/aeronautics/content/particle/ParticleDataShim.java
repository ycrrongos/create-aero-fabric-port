package dev.eriksonn.aeronautics.content.particle;

import com.zurrtum.create.foundation.particle.ICustomParticleData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public final class ParticleDataShim {
    private ParticleDataShim() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends ParticleOptions> ICustomParticleData.Deserializer<T> emptyDeserializer() {
        return (ICustomParticleData.Deserializer<T>) EMPTY;
    }

    public static void writeEmpty(final FriendlyByteBuf buf) {}

    private static final ICustomParticleData.Deserializer<ParticleOptions> EMPTY = new ICustomParticleData.Deserializer<>() {
        @Override
        public ParticleOptions fromCommand(final ParticleType<ParticleOptions> type,
                                           final com.mojang.brigadier.StringReader reader) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ParticleOptions fromNetwork(final ParticleType<ParticleOptions> type, final FriendlyByteBuf buf) {
            throw new UnsupportedOperationException();
        }
    };
}
