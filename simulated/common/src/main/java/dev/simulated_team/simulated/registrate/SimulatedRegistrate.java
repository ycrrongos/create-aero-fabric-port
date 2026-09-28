package dev.simulated_team.simulated.registrate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Minimal Create Fly registrate facade. */
public class SimulatedRegistrate extends CreateRegistrate {
    public SimulatedRegistrate(Identifier id, String modid) {
        super(modid);
    }

    public SimulatedRegistrate(String modid) {
        super(modid);
    }

    public SimulatedRegistrate defaultCreativeTab(ResourceKey<?> key) {
        return this;
    }

    public <T> Codec<T> byNameCodecExpanded(final ResourceKey<? extends Registry<T>> key) {
        return Identifier.CODEC.flatXmap(resourceLoc -> {
            for (final RegistryEntry<T, T> entry : this.getAll(key)) {
                if (entry.getId().equals(resourceLoc)) {
                    return DataResult.success(entry.get());
                }
            }
            return DataResult.error(() -> "Unknown registry element in " + key + ":" + resourceLoc);
        }, value -> {
            for (final RegistryEntry<T, T> entry : this.getAll(key)) {
                if (entry.is(value)) {
                    return DataResult.success(entry.getId());
                }
            }
            return DataResult.error(() -> "Unknown registry element in " + key + ":" + value);
        });
    }
}
