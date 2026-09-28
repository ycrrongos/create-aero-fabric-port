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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Quaternionf;

import java.util.function.Function;

public record GustParticleData(Quaternionf orientation) implements ParticleOptions, ICustomParticleDataWithSprite<GustParticleData> {

    private static final MapCodec<GustParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("x").forGetter(o -> o.orientation.x),
            Codec.FLOAT.fieldOf("y").forGetter(o -> o.orientation.y),
            Codec.FLOAT.fieldOf("z").forGetter(o -> o.orientation.z),
            Codec.FLOAT.fieldOf("w").forGetter(o -> o.orientation.w)
    ).apply(instance, (x, y, z, w) -> new GustParticleData(new Quaternionf(x, y, z, w))));

    private static final StreamCodec<RegistryFriendlyByteBuf, GustParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, o -> o.orientation.x,
            ByteBufCodecs.FLOAT, o -> o.orientation.y,
            ByteBufCodecs.FLOAT, o -> o.orientation.z,
            ByteBufCodecs.FLOAT, o -> o.orientation.w,
            (x, y, z, w) -> new GustParticleData(new Quaternionf(x, y, z, w))
    );

    public GustParticleData() {
        this(new Quaternionf());
    }

    @Override
    public Function<SpriteSet, ParticleProvider<GustParticleData>> getMetaFactory() {
        return sprites -> null;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buf) {
        ParticleDataShim.writeEmpty(buf);
    }

    @Override
    public ICustomParticleData.Deserializer<GustParticleData> getDeserializer() {
        return ParticleDataShim.emptyDeserializer();
    }

    @Override
    public MapCodec<GustParticleData> getCodec(final ParticleType<GustParticleData> type) {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, GustParticleData> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.GUST.get();
    }
}
