package dev.simulated_team.simulated.api.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record SimSoundEntry(Identifier id, SoundEvent sound, SoundSource category) {

    public SoundEvent event() {
        return this.sound;
    }

    public void play(final Level level, final Player player, final BlockPos pos, final float volume, final float pitch) {
        if (level != null) {
            level.playSound(player, pos, this.sound, this.category, volume, pitch);
        }
    }
}
