package net.minecraft.resources;

/**
 * Compile-time shim: Veil 4.5 common jars still reference {@code ResourceLocation}
 * while Minecraft 1.21.11 Mojmap renamed it to {@link Identifier}.
 * Fabric Loom remaps Veil at runtime; exclude this class from the Fabric remapped jar.
 */
@Deprecated
public final class ResourceLocation {
    private ResourceLocation() {}
}
