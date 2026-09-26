package dev.ryanhcode.sable.mixin.debug_render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ryanhcode.sable.mixinhelpers.debug_render.SableDebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds the Sable information to the F3 debug screen.
 * <p>
 * In 1.21.11 the lines of the debug screen are no longer built by {@code DebugScreenOverlay#getSystemInformation}, but by
 * the {@link DebugScreenEntry debug screen entries} enabled in the current debug profile. The Sable lines are provided by
 * the {@link SableDebugScreenEntry}, which is shown in the debug overlay of the default profile.
 */
@Mixin(DebugScreenEntries.class)
public abstract class DebugScreenOverlayMixin {

    @Shadow
    private static Identifier register(final Identifier name, final DebugScreenEntry entry) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void sable$registerDebugEntry(final CallbackInfo ci) {
        register(SableDebugScreenEntry.ID, new SableDebugScreenEntry());
    }

    /**
     * Shows the Sable entry in the debug overlay of the default debug profile.
     */
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;", remap = false))
    private static Map<Identifier, DebugScreenEntryStatus> sable$showInDefaultProfile(final Map<Identifier, DebugScreenEntryStatus> original) {
        final Map<Identifier, DebugScreenEntryStatus> defaultProfile = new HashMap<>(original);
        defaultProfile.put(SableDebugScreenEntry.ID, DebugScreenEntryStatus.IN_OVERLAY);
        return Map.copyOf(defaultProfile);
    }
}
