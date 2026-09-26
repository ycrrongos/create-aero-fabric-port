package dev.ryanhcode.sable.api.physics.force;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Default force groups. Uses plain suppliers instead of Veil {@code RegistryObject}
 * so common can compile against Mojmap 1.21.11 without a ResourceLocation shim.
 */
public final class ForceGroups {
    private ForceGroups() {}

    public static final ForceGroup GRAVITY_VALUE = new ForceGroup(Component.translatable("force_group.sable.gravity"), null, 0x216e55, false);
    public static final ForceGroup DRAG_VALUE = new ForceGroup(Component.translatable("force_group.sable.drag"), null, 0x834f31, false);
    public static final ForceGroup LEVITATION_VALUE = new ForceGroup(Component.translatable("force_group.sable.levitation"), null, 0x734480, true);
    public static final ForceGroup BALLOON_LIFT_VALUE = new ForceGroup(Component.translatable("force_group.sable.balloon_lift"), null, 0xd2643e, true);
    public static final ForceGroup PROPULSION_VALUE = new ForceGroup(Component.translatable("force_group.sable.propulsion"), null, 0x5a7c9f, true);
    public static final ForceGroup LIFT_VALUE = new ForceGroup(Component.translatable("force_group.sable.lift"), null, 0x8cb6c6, true);
    public static final ForceGroup MAGNETIC_FORCE_VALUE = new ForceGroup(Component.translatable("force_group.sable.magnetic_force"), null, 0xe05343, false);

    public static final Supplier<ForceGroup> GRAVITY = () -> GRAVITY_VALUE;
    public static final Supplier<ForceGroup> DRAG = () -> DRAG_VALUE;
    public static final Supplier<ForceGroup> LEVITATION = () -> LEVITATION_VALUE;
    public static final Supplier<ForceGroup> BALLOON_LIFT = () -> BALLOON_LIFT_VALUE;
    public static final Supplier<ForceGroup> PROPULSION = () -> PROPULSION_VALUE;
    public static final Supplier<ForceGroup> LIFT = () -> LIFT_VALUE;
    public static final Supplier<ForceGroup> MAGNETIC_FORCE = () -> MAGNETIC_FORCE_VALUE;

    private static final List<ForceGroup> ALL = List.of(
            GRAVITY_VALUE, DRAG_VALUE, LEVITATION_VALUE, BALLOON_LIFT_VALUE, PROPULSION_VALUE, LIFT_VALUE, MAGNETIC_FORCE_VALUE
    );

    public static void register() {
        // no-op
    }

    public static int count() {
        return ALL.size();
    }
}
