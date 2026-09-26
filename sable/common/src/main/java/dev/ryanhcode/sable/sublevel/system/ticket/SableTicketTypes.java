package dev.ryanhcode.sable.sublevel.system.ticket;

import dev.ryanhcode.sable.Sable;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.TicketType;

/**
 * Custom chunk tickets for Sable sub-level force-loading (1.21.11+ TicketType API).
 */
public final class SableTicketTypes {
    private SableTicketTypes() {}

    /**
     * Loads and simulates chunks inhabited by force-loaded sub-levels.
     * Registered into {@link BuiltInRegistries#TICKET_TYPE}.
     */
    public static final TicketType SUB_LEVEL_LOADED = Registry.register(
            BuiltInRegistries.TICKET_TYPE,
            Sable.sablePath("sub_level_loaded"),
            new TicketType(
                    TicketType.NO_TIMEOUT,
                    TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION
            )
    );

    /** Touch this class early so the ticket type is registered before use. */
    public static void bootstrap() {
        // static init
    }
}
