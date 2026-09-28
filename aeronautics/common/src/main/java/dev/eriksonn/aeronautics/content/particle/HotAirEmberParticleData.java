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

public class HotAirEmberParticleData implements ParticleOptions, ICustomParticleDataWithSprite<HotAirEmberParticleData> {

    private static final MapCodec<HotAirEmberParticleData> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                    Codec.BOOL.fieldOf("isSoul").forGetter((p) -> p.isSoul)
    ).apply(i, HotAirEmberParticleData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, HotAirEmberParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (p) -> p.isSoul,
            HotAirEmberParticleData::new);

    protected final boolean isSoul;

    public HotAirEmberParticleData(final boolean isSoul) {
        this.isSoul = isSoul;
    }

    public HotAirEmberParticleData() {
        this.isSoul = false;
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.HOT_AIR_EMBER.get();
    }

    @Override
    public Function<SpriteSet, ParticleProvider<HotAirEmberParticleData>> getMetaFactory() {
        return sprites -> null;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buf) {
        ParticleDataShim.writeEmpty(buf);
    }

    @Override
    public ICustomParticleData.Deserializer<HotAirEmberParticleData> getDeserializer() {
        return ParticleDataShim.emptyDeserializer();
    }

    @Override
    public MapCodec<HotAirEmberParticleData> getCodec(final ParticleType<HotAirEmberParticleData> particleType) {
        return CODEC;
    }
}
