package dev.simulated_team.simulated.api.sound;

import com.mojang.serialization.Codec;

public record SoundFile(String name) {
    public static final Codec<SoundFile> FULL_CODEC = Codec.STRING.xmap(SoundFile::new, SoundFile::name);
}
