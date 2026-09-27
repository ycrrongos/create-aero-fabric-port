package dev.simulated_team.simulated.mixin.accessor;

import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;

public interface ControlledContraptionEntityAccessor {
    BlockPos getControllerPos();

    StructureTransform invokeMakeStructureTransform();
}
