package dev.simulated_team.simulated.index;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
public class SimRegistries {
    public static class Keys {
        public static final ResourceKey<Registry<Object>> NAVIGATION_TARGET =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("simulated", "navigation_target"));
    }
    public static final Registry<Object> NAVIGATION_TARGET = null;
    public static void register() {}
}
