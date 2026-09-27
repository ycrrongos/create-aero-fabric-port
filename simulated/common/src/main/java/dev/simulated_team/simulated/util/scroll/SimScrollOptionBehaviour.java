package dev.simulated_team.simulated.util.scroll;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

/** Concrete ScrollOptionBehaviour with getValue/setValue for Simulated call sites. */
public class SimScrollOptionBehaviour<T extends Enum<T> & INamedIconOptions> extends ScrollOptionBehaviour<T> {

    public SimScrollOptionBehaviour(final Class<T> options, final Component label, final SmartBlockEntity be, final ValueBoxTransform slot) {
        super(options, Function.identity(), label, be, slot);
    }

    public int getValue() {
        return this.behaviour.getValue();
    }

    public void setValue(final int value) {
        this.behaviour.setValue(value);
    }
}
