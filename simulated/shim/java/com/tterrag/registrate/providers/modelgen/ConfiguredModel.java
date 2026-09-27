package com.tterrag.registrate.providers.modelgen;

/** Compile stand-in for NeoForge's ConfiguredModel builder. */
public final class ConfiguredModel {
    private ConfiguredModel() {}

    public static final class Builder<T> {
        public Builder<T> rotationY(final int y) {
            return this;
        }
    }
}
