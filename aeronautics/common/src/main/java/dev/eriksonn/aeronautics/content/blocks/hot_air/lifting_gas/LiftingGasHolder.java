package dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Codec holder stub — full registry codec restore later. */
public record LiftingGasHolder(LiftingGasType type, float amount) {
    public static final Codec<LiftingGasHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("amount").forGetter(LiftingGasHolder::amount)
            ).apply(instance, amount -> new LiftingGasHolder(null, amount)));
}
