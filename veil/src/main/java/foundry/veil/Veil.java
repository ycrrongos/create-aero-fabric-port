package foundry.veil;

import foundry.veil.platform.VeilPlatform;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Entry point of the Veil port for Minecraft 1.21.11 (Fabric).
 * <p>
 * This port provides the parts of Veil used by Sable, Create Simulated and Create Aeronautics, re-implemented on top of
 * the 1.21.11 render backend.
 */
public class Veil {

    public static final String MODID = "veil";
    public static final Logger LOGGER = LoggerFactory.getLogger("Veil");
    public static final boolean DEBUG;
    public static final boolean VERBOSE_SHADER_ERRORS;

    private static final VeilPlatform PLATFORM = ServiceLoader.load(VeilPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Veil expected platform implementation"));

    public static final boolean SODIUM = PLATFORM.isModLoaded("sodium");
    public static final boolean IRIS = PLATFORM.isModLoaded("iris");
    public static final boolean IMGUIMC = PLATFORM.isModLoaded("imguimc");

    static {
        DEBUG = System.getProperty("veil.debug") != null;
        VERBOSE_SHADER_ERRORS = System.getProperty("veil.verboseShaderErrors") != null;
    }

    @ApiStatus.Internal
    public static void init() {
        LOGGER.info("Veil is initializing.");
        if (DEBUG) {
            LOGGER.info("Veil Debug Enabled");
        }
    }

    public static Identifier veilPath(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static VeilPlatform platform() {
        return PLATFORM;
    }
}
