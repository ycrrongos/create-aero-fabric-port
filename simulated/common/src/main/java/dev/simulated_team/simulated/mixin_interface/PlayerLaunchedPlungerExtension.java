package dev.simulated_team.simulated.mixin_interface;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public interface PlayerLaunchedPlungerExtension {
    default Set<UUID> simulated$getLaunchedPlungers() { return Collections.emptySet(); }
    default void simulated$addLaunchedPlunger(UUID id) {}
    default void simulated$removeLaunchedPlunger(UUID id) {}
}
