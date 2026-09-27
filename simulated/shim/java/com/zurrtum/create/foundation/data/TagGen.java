package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.tags.TagKey;

import java.util.function.Function;

/** Minimal TagGen stand-in. */
public class TagGen {
    public static <T extends net.minecraft.world.level.block.Block, P> com.tterrag.registrate.util.nullness.NonNullUnaryOperator<com.tterrag.registrate.builders.BlockBuilder<T, P>> axeOrPickaxe() {
        return b -> b;
    }
    public static <T extends net.minecraft.world.level.block.Block, P> com.tterrag.registrate.util.nullness.NonNullUnaryOperator<com.tterrag.registrate.builders.BlockBuilder<T, P>> pickaxeOnly() {
        return b -> b;
    }

    public static class CreateTagsProvider<T> {
        private final RegistrateTagsProvider<T> prov;
        public CreateTagsProvider(final RegistrateTagsProvider<T> prov, final Function<T, ?> holder) {
            this.prov = prov;
        }
        public CreateTagsProvider<T> tag(final TagKey<T> key) {
            return this;
        }
        public CreateTagsProvider<T> addTag(final TagKey<T> key) {
            return this;
        }
        public CreateTagsProvider<T> add(final T... values) {
            return this;
        }
    }
}
