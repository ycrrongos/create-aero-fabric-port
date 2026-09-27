package dev.simulated_team.simulated.util.scroll;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.RotationDirectionScrollBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;

public class SimRotationDirectionScrollBehaviour extends RotationDirectionScrollBehaviour {
    public SimRotationDirectionScrollBehaviour(final SmartBlockEntity be, final Component label, final ValueBoxTransform slot) {
        super(be, label, slot);
    }

    public int getValue() {
        return this.behaviour.getValue();
    }

    public void setValue(final int value) {
        this.behaviour.setValue(value);
    }
}
