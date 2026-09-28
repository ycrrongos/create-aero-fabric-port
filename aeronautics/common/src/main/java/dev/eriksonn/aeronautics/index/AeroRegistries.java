package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.CrystalPropagationContext;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/** Keys only — Veil RegistrationProvider is incompatible with 1.21.11 Registry mappings. */
public class AeroRegistries {
    public static class Keys {
        public static final ResourceKey<Registry<LiftingGasType>> LIFTING_GAS_TYPE = key("lifting_gas_type");
        public static final ResourceKey<Registry<CrystalPropagationContext>> LEVITITE_CRYSTAL_PROPAGATION_CONTEXT =
                key("levitite_crystal_propagation_context");

        private static <T> ResourceKey<Registry<T>> key(final String name) {
            return ResourceKey.createRegistryKey(Aeronautics.path(name));
        }
    }

    public static void init() {}
}
