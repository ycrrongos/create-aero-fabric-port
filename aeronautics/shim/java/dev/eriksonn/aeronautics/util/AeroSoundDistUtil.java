package dev.eriksonn.aeronautics.util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
public final class AeroSoundDistUtil {
    private AeroSoundDistUtil() {}
    public static void play(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float vol, float pitch) {
        if (level != null && !level.isClientSide()) level.playSound(null, pos, sound, source, vol, pitch);
    }
    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float vol, float pitch) {
        if (level != null && !level.isClientSide()) level.playSound(null, x, y, z, sound, source, vol, pitch);
    }
    public static void removePosHotAirBurnerSound(BlockPos pos) {}
    public static void removePosSteamVentSound(BlockPos pos) {}
    public static void removePosPropellerSound(BlockPos pos) {}
    public static void addPosSteamVentSound(BlockPos pos) {}
    public static void addPosHotAirBurnerSound(BlockPos pos) {}
    public static void addPosPropellerSound(BlockPos pos) {}
    public static void tickHotAirBurnerSound(BlockPos pos, Level level, float pitch) {}
    public static void tickSteamVentSound(BlockPos pos, Level level, float pitch) {}
    public static void tickPropellerSound(BlockPos pos, Level level, float pitch) {}

    public static Object tickPropellerSounds(final Object be, final Object current) {
        return null;
    }
}
