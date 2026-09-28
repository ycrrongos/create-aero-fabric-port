package dev.eriksonn.aeronautics.content.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.foundation.particle.ICustomParticleData;
import com.zurrtum.create.foundation.particle.ICustomParticleDataWithSprite;
import net.minecraft.network.FriendlyByteBuf;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

import java.util.function.Function;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class LevititeSparkleParticleData implements ParticleOptions, ICustomParticleDataWithSprite<LevititeSparkleParticleData> {
    public static final int LEVITITE_GREEN = 9424022;
    public static final int LEVITITE_PINK = 15521489;

    public static final MapCodec<LevititeSparkleParticleData> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(p -> p.color)
            ).apply(instance, LevititeSparkleParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LevititeSparkleParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.color,
            LevititeSparkleParticleData::new
    );

    public final int color;

    public LevititeSparkleParticleData(final int color) {
        this.color = color;
    }

    public LevititeSparkleParticleData() {
        this(LEVITITE_GREEN);
    }

    @Override
    public Function<SpriteSet, ParticleProvider<LevititeSparkleParticleData>> getMetaFactory() {
        return sprites -> null;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buf) {
        ParticleDataShim.writeEmpty(buf);
    }

    @Override
    public ICustomParticleData.Deserializer<LevititeSparkleParticleData> getDeserializer() {
        return ParticleDataShim.emptyDeserializer();
    }

    @Override
    public MapCodec<LevititeSparkleParticleData> getCodec(final ParticleType<LevititeSparkleParticleData> type) {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, LevititeSparkleParticleData> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.LEVITITE_SPARKLE.get();
    }
}
