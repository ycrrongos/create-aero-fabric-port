package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.simulated_team.simulated.api.sound.SimSoundEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Register sound events into BuiltInRegistries (Sim SoundEventRegistry does not). */
public class AeroSoundEvents {
    public static final SimSoundEntry CLOUD_SKIPPER_TRANSFORM =
            register("item.cloud_skipper_transform", SoundSource.AMBIENT);
    public static final SimSoundEntry MUSIC_DISC_CLOUD_SKIPPER =
            register("music_disc.cloud_skipper", SoundSource.RECORDS);
    public static final SimSoundEntry HOT_AIR_BURNER_HEAT =
            register("block.hot_air_burner.head", SoundSource.BLOCKS);
    public static final SimSoundEntry HOT_AIR_BURNER_IDLE =
            register("block.hot_air_burner.idle", SoundSource.BLOCKS);
    public static final SimSoundEntry STEAM_VENT_HEAT =
            register("block.steam_vent.head", SoundSource.BLOCKS);
    public static final SimSoundEntry STEAM_VENT_IDLE =
            register("block.steam_vent.idle", SoundSource.BLOCKS);
    public static final SimSoundEntry STEAM_VENT_OPEN =
            register("block.steam_vent.open", SoundSource.BLOCKS);
    public static final SimSoundEntry STEAM_VENT_CLOSE =
            register("block.steam_vent.close", SoundSource.BLOCKS);
    public static final SimSoundEntry PROPELLER_LARGE_LOOP =
            register("block.propeller_bearing.large_loop", SoundSource.BLOCKS);
    public static final SimSoundEntry PROPELLER_SMALL_LOOP =
            register("block.propeller_bearing.small_loop", SoundSource.BLOCKS);
    public static final SimSoundEntry GUST =
            register("entity.gust", SoundSource.BLOCKS);

    private static SimSoundEntry register(final String path, final SoundSource category) {
        final Identifier id = Aeronautics.path(path);
        final SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        return new SimSoundEntry(id, event, category);
    }

    public static void init() {}
}
