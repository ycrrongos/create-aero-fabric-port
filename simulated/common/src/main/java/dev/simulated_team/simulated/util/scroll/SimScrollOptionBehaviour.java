package dev.simulated_team.simulated.util.scroll;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

/** Concrete ScrollOptionBehaviour with getValue/setValue/get for Simulated call sites. */
public class SimScrollOptionBehaviour<T extends Enum<T> & INamedIconOptions> extends ScrollOptionBehaviour<T> {

    private final Class<T> options;

    public SimScrollOptionBehaviour(final Class<T> options, final Component label, final SmartBlockEntity be, final ValueBoxTransform slot) {
        super(options, Function.identity(), label, be, slot);
        this.options = options;
    }

    public int getValue() {
        return this.behaviour.getValue();
    }

    public void setValue(final int value) {
        this.behaviour.setValue(value);
    }

    public T get() {
        final T[] constants = this.options.getEnumConstants();
        final int idx = Math.floorMod(this.getValue(), constants.length);
        return constants[idx];
    }
}
