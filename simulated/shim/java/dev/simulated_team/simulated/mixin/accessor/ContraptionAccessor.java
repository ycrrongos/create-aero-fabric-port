package dev.simulated_team.simulated.mixin.accessor;

import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.world.phys.AABB;

import java.util.List;

public interface ContraptionAccessor {
    List<AABB> getSuperGlue();
}
