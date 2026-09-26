package dev.ryanhcode.sable.physics.config.block_properties;

import com.mojang.serialization.Codec;
import dev.ryanhcode.sable.Sable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Default physics block properties — plain map, no Veil {@code RegistrationProvider}
 * (Veil 1.21.1 Registry hooks crash on 1.21.11).
 */
public final class PhysicsBlockPropertyTypes {
    private PhysicsBlockPropertyTypes() {}

    private static final Map<Identifier, PhysicsBlockPropertyType<?>> BY_ID = new LinkedHashMap<>();

    public static final Supplier<PhysicsBlockPropertyType<Double>> MASS = register(Sable.sablePath("mass"), Codec.DOUBLE, 1.0);
    public static final Supplier<PhysicsBlockPropertyType<Vec3>> INERTIA = register(Sable.sablePath("inertia"), Vec3.CODEC, null);
    public static final Supplier<PhysicsBlockPropertyType<Double>> VOLUME = register(Sable.sablePath("volume"), Codec.DOUBLE, 1.0);
    public static final Supplier<PhysicsBlockPropertyType<Double>> RESTITUTION = register(Sable.sablePath("restitution"), Codec.DOUBLE, 0.0);
    public static final Supplier<PhysicsBlockPropertyType<Double>> FRICTION = register(Sable.sablePath("friction"), Codec.DOUBLE, 1.0);
    public static final Supplier<PhysicsBlockPropertyType<Boolean>> FRAGILE = register(Sable.sablePath("fragile"), Codec.BOOL, false);
    public static final Supplier<PhysicsBlockPropertyType<Identifier>> FLOATING_MATERIAL = register(Sable.sablePath("floating_material"), Identifier.CODEC, null);
    public static final Supplier<PhysicsBlockPropertyType<Double>> FLOATING_SCALE = register(Sable.sablePath("floating_scale"), Codec.DOUBLE, 1.0);

    public static void register() {
        // static init already registered defaults
    }

    private static <T> Supplier<PhysicsBlockPropertyType<T>> register(final Identifier id, final Codec<T> codec, final T defaultValue) {
        if (BY_ID.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate physics block property: %s".formatted(id));
        }
        final PhysicsBlockPropertyType<T> type = new PhysicsBlockPropertyType<>(BY_ID.size(), codec, defaultValue);
        BY_ID.put(id, type);
        return () -> type;
    }

    public static int count() {
        return BY_ID.size();
    }

    public static Codec<Object> getPropertyCodec(final Identifier id) {
        final PhysicsBlockPropertyType<?> property = BY_ID.get(id);
        if (property != null) {
            //noinspection unchecked
            return (Codec<Object>) property.codec;
        }
        throw new IllegalArgumentException("Unknown physics block property: %s".formatted(id));
    }

    public static PhysicsBlockPropertyType<?> getPropertyType(final Identifier id) {
        final PhysicsBlockPropertyType<?> property = BY_ID.get(id);
        if (property != null) {
            return property;
        }
        throw new IllegalArgumentException("Unknown physics block property: %s".formatted(id));
    }

    public record PhysicsBlockPropertyType<T>(int id, Codec<T> codec, T defaultValue) {
    }
}
