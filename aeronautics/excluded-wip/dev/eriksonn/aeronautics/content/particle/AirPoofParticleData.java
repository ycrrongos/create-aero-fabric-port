package dev.eriksonn.aeronautics.content.particle;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.foundation.particle.ICustomParticleData;
import com.zurrtum.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

import java.util.function.Function;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class AirPoofParticleData implements ParticleOptions, ICustomParticleDataWithSprite<AirPoofParticleData> {
    public static final AirPoofParticleData INSTANCE = new AirPoofParticleData();
    private static final MapCodec<AirPoofParticleData> CODEC = MapCodec.unit(INSTANCE);
    private static final StreamCodec<FriendlyByteBuf, AirPoofParticleData> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private AirPoofParticleData() {}

    @Override
    public Function<SpriteSet, ParticleProvider<AirPoofParticleData>> getMetaFactory() {
        return sprites -> null;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buf) {
        ParticleDataShim.writeEmpty(buf);
    }

    @Override
    public ICustomParticleData.Deserializer<AirPoofParticleData> getDeserializer() {
        return ParticleDataShim.emptyDeserializer();
    }

    @Override
    public MapCodec<AirPoofParticleData> getCodec(final ParticleType<AirPoofParticleData> type) {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, AirPoofParticleData> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.AIR_POOF.get();
    }
}
