package dev.simulated_team.simulated.api.sound;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class SoundEventRegistry {
    private final String modId;

    public SoundEventRegistry(final String modId) {
        this.modId = modId;
    }

    public SimSoundEntry create(final String name, final UnaryOperator<DefinitionBuilder> operator) {
        return this.create(name, SoundSource.BLOCKS, operator);
    }

    public SimSoundEntry create(final String name, final SoundSource category, final UnaryOperator<DefinitionBuilder> operator) {
        operator.apply(new DefinitionBuilder());
        final Identifier id = Identifier.fromNamespaceAndPath(this.modId, name.replace('.', '_'));
        return new SimSoundEntry(id, SoundEvent.createVariableRangeEvent(id), category);
    }

    public void provideLang(final BiConsumer<String, String> consumer) {}

    public static final class DefinitionBuilder {
        public DefinitionBuilder defaultSubtitle(final String key) {
            return this;
        }

        public DefinitionBuilder subtitle(final String subtitle) {
            return this;
        }

        public DefinitionBuilder addFileVariants(final String path, final int count) {
            return this;
        }

        public DefinitionBuilder addFileVariant(final String path, final Consumer<Object> operator) {
            return this;
        }

        public DefinitionBuilder addFileVariant(final net.minecraft.resources.Identifier path, final UnaryOperator<Object> operator) {
            return this;
        }

        public DefinitionBuilder addEventVariant(final Object sound) {
            return this;
        }

        public DefinitionBuilder addEventVariant(final Object sound, final UnaryOperator<Object> operator) {
            return this;
        }
    }
}
