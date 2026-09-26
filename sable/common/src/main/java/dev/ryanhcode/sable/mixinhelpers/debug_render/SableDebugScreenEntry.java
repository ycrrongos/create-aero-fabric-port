package dev.ryanhcode.sable.mixinhelpers.debug_render;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.render.dispatcher.SubLevelRenderDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Sable section of the F3 debug screen, containing the client sub-level container and sub-level renderer information.
 * <p>
 * Registered in {@link net.minecraft.client.gui.components.debug.DebugScreenEntries} as {@link #ID}, and shown in the
 * debug overlay of the default debug profile.
 */
public class SableDebugScreenEntry implements DebugScreenEntry {

    public static final Identifier ID = Sable.sablePath("sable");

    @Override
    public void display(final DebugScreenDisplayer displayer, @Nullable final Level level, @Nullable final LevelChunk clientChunk, @Nullable final LevelChunk serverChunk) {
        final List<String> lines = new ArrayList<>();
        final SubLevelContainer container = SubLevelContainer.getContainer(Minecraft.getInstance().level);

        lines.add(ChatFormatting.UNDERLINE + "Sable");
        if (container instanceof final ClientSubLevelContainer clientContainer) {
            clientContainer.addDebugInfo(lines::add);
        }
        SubLevelRenderDispatcher.get().addDebugInfo(lines::add);

        displayer.addToGroup(ID, lines);
    }

    /**
     * The Sable debug information was always shown, even with reduced debug info.
     */
    @Override
    public boolean isAllowed(final boolean reducedDebugInfo) {
        return true;
    }
}
