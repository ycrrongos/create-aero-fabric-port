package dev.eriksonn.aeronautics.index;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Advancement wiring deferred; gameplay awards are no-ops until datagen port lands. */
public final class AeroAdvancements {
    private AeroAdvancements() {}

    public static final Adv ROOT = new Adv();
    public static final Adv HIGH_FASHION = new Adv();
    public static final Adv HEAD_IN_THE_CLOUDS = new Adv();
    public static final Adv SONG_OF_THE_SKY = new Adv();
    public static final Adv FOR_EVERY_ACTION = new Adv();
    public static final Adv IN_THRUST_WE_TRUST = new Adv();
    public static final Adv HEAVIER_ARTILLERY = new Adv();
    public static final Adv NOW_AVAILABLE_IN_PINK = new Adv();
    public static final Adv UNIDENTIFIED_FLOATING_OBJECT = new Adv();
    public static final Adv GHOSTBUSTER = new Adv();

    public static final class Adv {
        public void awardToNearby(final BlockPos pos, final Level level) {}

        public void awardToNearby(final BlockPos pos, final Level level, final double radius) {}
    }
}
