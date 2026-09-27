package com.tterrag.registrate.providers.modelgen;

import net.minecraft.world.level.block.state.properties.Property;

/** Compile stand-in for NeoForge multipart blockstate builders. */
public class MultiPartBlockStateBuilder {
    public PartBuilder part() {
        return new PartBuilder(this);
    }

    public static final class PartBuilder {
        private final MultiPartBlockStateBuilder owner;

        PartBuilder(final MultiPartBlockStateBuilder owner) {
            this.owner = owner;
        }

        public PartBuilder modelFile(final ModelFile file) {
            return this;
        }

        public PartBuilder rotationX(final int x) {
            return this;
        }

        public PartBuilder rotationY(final int y) {
            return this;
        }

        public PartBuilder addModel() {
            return this;
        }

        public PartBuilder condition(final Property<?> property, final Comparable<?> value) {
            return this;
        }

        public MultiPartBlockStateBuilder end() {
            return this.owner;
        }
    }
}
