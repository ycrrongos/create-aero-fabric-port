package com.zurrtum.create.content.contraptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.core.Direction;
import java.util.function.Predicate;
public class DirectionalExtenderScrollOptionSlot extends ValueBoxTransform.Sided {
    private final Predicate<Direction> allowed;
    public DirectionalExtenderScrollOptionSlot(Predicate<Direction> allowedDirections) {
        this.allowed = allowedDirections;
    }
}
