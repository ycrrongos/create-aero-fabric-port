package dev.simulated_team.simulated;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimEntityTypes;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/** Fabric/Create Fly entry bootstrap (trimmed until full port compiles). */
public final class Simulated {
    public static final String MOD_ID = "simulated";
    public static final String MOD_NAME = "Create Simulated";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final SimulatedRegistrate REGISTRATE_INSTANCE =
            new SimulatedRegistrate(path("simulated"), MOD_ID);
    private static final NonNullSupplier<SimulatedRegistrate> REGISTRATE = () -> REGISTRATE_INSTANCE;

    private Simulated() {}

    public static void init() {
        LOGGER.info("Initializing {}", MOD_NAME);
        try { SimTags.addGenerators(); } catch (Throwable t) { LOGGER.warn("SimTags: {}", t.toString()); }
        try { SimBlocks.register(); } catch (Throwable t) { LOGGER.warn("SimBlocks: {}", t.toString()); }
        try { SimItems.register(); } catch (Throwable t) { LOGGER.warn("SimItems: {}", t.toString()); }
        try { SimBlockEntityTypes.register(); } catch (Throwable t) { LOGGER.warn("SimBlockEntityTypes: {}", t.toString()); }
        try { SimEntityTypes.register(); } catch (Throwable t) { LOGGER.warn("SimEntityTypes: {}", t.toString()); }
    }

    public static SimulatedRegistrate getRegistrate() {
        return REGISTRATE.get();
    }

    public static Identifier path(final String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
