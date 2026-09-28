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

public class PropellerAirParticleData implements ParticleOptions, ICustomParticleDataWithSprite<PropellerAirParticleData> {
    public static final float FRICTION_SCALE = 0.95f;
    public static final float LIFE_TIME = 20f;

    private static final MapCodec<PropellerAirParticleData> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                    Codec.BOOL.fieldOf("collision").forGetter((p) -> p.enableCollision),
                    Codec.BOOL.fieldOf("virtual").forGetter(p -> p.isVirtual))
            .apply(i, PropellerAirParticleData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, PropellerAirParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (p) -> p.enableCollision,
            ByteBufCodecs.BOOL, (p) -> p.isVirtual,
            PropellerAirParticleData::new);


    boolean enableCollision;
    boolean isVirtual;

    public PropellerAirParticleData(boolean enableCollision, boolean isVirtual) {
        this.enableCollision = enableCollision;
        this.isVirtual = isVirtual;
    }

    public PropellerAirParticleData() {
        this(true, false);
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.PROPELLER_AIR_FLOW.get();
    }

    @Override
    public Function<SpriteSet, ParticleProvider<PropellerAirParticleData>> getMetaFactory() {
        return sprites -> null;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buf) {
        ParticleDataShim.writeEmpty(buf);
    }

    @Override
    public ICustomParticleData.Deserializer<PropellerAirParticleData> getDeserializer() {
        return ParticleDataShim.emptyDeserializer();
    }

    @Override
    public MapCodec<PropellerAirParticleData> getCodec(ParticleType<PropellerAirParticleData> particleType) {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, PropellerAirParticleData> getStreamCodec() {
        return STREAM_CODEC;
    }
}
