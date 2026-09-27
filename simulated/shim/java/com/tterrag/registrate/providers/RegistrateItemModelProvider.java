package com.tterrag.registrate.providers;

import net.minecraft.resources.Identifier;

/** Named stand-in so item-model datagen lambdas type-check. */
public class RegistrateItemModelProvider {
    public Object getBuilder(String path) {
        return null;
    }

    public Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath("simulated", path);
    }

    public ModelBuilder withExistingParent(String name, Identifier parent) {
        return new ModelBuilder();
    }

    public ModelBuilder withExistingParent(String name, String parent) {
        return new ModelBuilder();
    }

    public static final class ModelBuilder {
        public ModelBuilder texture(String key, Identifier texture) {
            return this;
        }

        public ModelBuilder texture(String key, String texture) {
            return this;
        }
    }
}
