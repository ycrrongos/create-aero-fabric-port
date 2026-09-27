package dev.simulated_team.simulated.mixin_interface;

public interface PlayerTypewriterExtension {
    default Object simulated$getTypewriter() { return null; }
    default void simulated$setTypewriter(Object be) {}
}
