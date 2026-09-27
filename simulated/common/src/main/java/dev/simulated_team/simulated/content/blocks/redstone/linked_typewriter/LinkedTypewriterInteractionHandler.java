package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter;

/** Compile stub until full interaction port is restored. */
public class LinkedTypewriterInteractionHandler {
    public enum Mode { NONE, BINDING_FROM_ITEM, BINDING_FROM_BLOCK }
    private static Mode mode = Mode.NONE;
    public static Mode getMode() { return mode; }
    public static void setMode(Mode m) { mode = m; }
}
