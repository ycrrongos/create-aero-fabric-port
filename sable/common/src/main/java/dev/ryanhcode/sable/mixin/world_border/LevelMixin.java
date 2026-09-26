package dev.ryanhcode.sable.mixin.world_border;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ryanhcode.sable.mixinterface.world_border.WorldBorderExtension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Gives the world border of server levels access to their level, so that sub-level plots are always considered in bounds.
 * <p>
 * In 1.21.11 the world border is no longer a field of {@code Level}. Server levels store it as saved data in their
 * {@link net.minecraft.world.level.storage.DimensionDataStorage}, lazily created (or migrated from legacy level data) in
 * {@link ServerLevel#getWorldBorder()}, so the level is attached whenever it is handed out.
 * The client level counterpart is {@link ClientLevelMixin}.
 */
@Mixin(ServerLevel.class)
public class LevelMixin {

    @ModifyReturnValue(method = "getWorldBorder", at = @At("RETURN"))
    private WorldBorder sable$initializeWorldBorder(final WorldBorder worldBorder) {
        ((WorldBorderExtension) worldBorder).sable$setLevel((ServerLevel) (Object) this);
        return worldBorder;
    }

}
