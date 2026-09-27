package dev.simulated_team.simulated.util.scroll;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Create Fly split scroll values into client UI + server storage.
 * Concrete non-generic surface matching Simulated's old ScrollValueBehaviour usage.
 */
public class SimScrollValueBehaviour extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {

    public SimScrollValueBehaviour(final Component label, final SmartBlockEntity be, final ValueBoxTransform slot) {
        super(label, be, slot);
    }

    public int getValue() {
        return this.behaviour.getValue();
    }

    public void setValue(final int value) {
        this.behaviour.setValue(value);
    }

    public SimScrollValueBehaviour between(final int min, final int max) {
        this.behaviour.between(min, max);
        return this;
    }

    public SimScrollValueBehaviour withCallback(final Consumer<Integer> callback) {
        this.behaviour.withCallback(callback);
        return this;
    }
}
