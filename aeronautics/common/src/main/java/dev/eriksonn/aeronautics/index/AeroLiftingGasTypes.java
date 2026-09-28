package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.DefaultLiftingGas;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.SteamLiftingGas;

/** Server-safe lifting gas holders (no client/registrate self()). */
public class AeroLiftingGasTypes {

    public static final DefaultLiftingGas DEFAULT_GAS = new DefaultLiftingGas();
    public static final SteamLiftingGas STEAM = new SteamLiftingGas();

    public static LiftingGasType defaultGas() {
        return DEFAULT_GAS;
    }

    public static LiftingGasType steam() {
        return STEAM;
    }

    public static void init() {}
}
