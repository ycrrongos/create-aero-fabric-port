package dev.simulated_team.simulated.util.scroll;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.BrassDiodeScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

/** Brass diode scroll with getValue/setValue/between for Simulated. */
public class SimBrassDiodeScrollValueBehaviour extends BrassDiodeScrollValueBehaviour {

    public SimBrassDiodeScrollValueBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    public int getValue() {
        return this.behaviour.getValue();
    }

    public void setValue(final int value) {
        this.behaviour.setValue(value);
    }

    public SimBrassDiodeScrollValueBehaviour between(final int min, final int max) {
        this.behaviour.between(min, max);
        return this;
    }
}
