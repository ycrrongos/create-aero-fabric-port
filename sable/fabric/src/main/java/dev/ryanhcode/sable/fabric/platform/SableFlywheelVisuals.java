package dev.ryanhcode.sable.fabric.platform;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

/**
 * Bridge to the Flywheel visualization system bundled with Create Fly. The Create compatibility layer installs the
 * handler when Create is present, so single-block sub-levels get their block entity visuals created.
 */
@ApiStatus.Internal
public final class SableFlywheelVisuals {

    private static Consumer<BlockEntity> handler = blockEntity -> {
    };

    private SableFlywheelVisuals() {
    }

    public static void setHandler(final Consumer<BlockEntity> handler) {
        SableFlywheelVisuals.handler = handler;
    }

    public static void tryAddVisual(final BlockEntity blockEntity) {
        handler.accept(blockEntity);
    }
}
