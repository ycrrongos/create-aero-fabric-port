package dev.eriksonn.aeronautics.index;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.zurrtum.create.foundation.block.DyedBlockList;
import com.zurrtum.create.foundation.data.SharedProperties;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.hot_air.envelope.EnvelopeBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.envelope.EnvelopeEncasedShaftBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.steam_vent.SteamVentBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.smart_propeller.SmartPropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden.WoodenPropellerBlock;
import dev.eriksonn.aeronautics.registry.AeroRegistrate;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Fabric registration without NeoForge datagen.
 * Avoid BlockBuilder.properties(...) — railways/create-fly expose Function, our shim used UnaryOperator.
 */
public class AeroBlocks {
    private static final AeroRegistrate REGISTRATE = Aeronautics.getRegistrate();

    public static final BlockEntry<EnvelopeBlock> WHITE_ENVELOPE_BLOCK = envelope(DyeColor.WHITE);

    public static final DyedBlockList<EnvelopeBlock> DYED_ENVELOPE_BLOCKS = new DyedBlockList<>(color ->
            color == DyeColor.WHITE ? WHITE_ENVELOPE_BLOCK : envelope(color));

    private static BlockEntry<EnvelopeBlock> envelope(DyeColor color) {
        String name = color == DyeColor.WHITE ? "white_envelope" : color.getName() + "_envelope";
        return REGISTRATE.block(name, p -> new EnvelopeBlock(
                        p.mapColor(color).sound(SoundType.WOOL).noOcclusion(), color))
                .initialProperties(SharedProperties::wooden)
                .item().build().register();
    }

    public static final DyedBlockList<EnvelopeEncasedShaftBlock> ENVELOPE_ENCASED_SHAFTS = new DyedBlockList<>(color ->
            REGISTRATE.block(color.getName() + "_envelope_encased_shaft",
                            p -> new EnvelopeEncasedShaftBlock(p.noOcclusion(), color))
                    .initialProperties(SharedProperties::wooden)
                    .item().build().register());

    public static final BlockEntry<HotAirBurnerBlock> HOT_AIR_BURNER = REGISTRATE
            .block("hot_air_burner", HotAirBurnerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<SteamVentBlock> STEAM_VENT = REGISTRATE
            .block("steam_vent", SteamVentBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<PropellerBearingBlock> PROPELLER_BEARING = REGISTRATE
            .block("propeller_bearing", PropellerBearingBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<GyroscopicPropellerBearingBlock> GYROSCOPIC_PROPELLER_BEARING = REGISTRATE
            .block("gyroscopic_propeller_bearing", GyroscopicPropellerBearingBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<WoodenPropellerBlock> WOODEN_PROPELLER = REGISTRATE
            .block("wooden_propeller", WoodenPropellerBlock::new)
            .initialProperties(SharedProperties::wooden)
            .item().build().register();

    public static final BlockEntry<AndesitePropellerBlock> ANDESITE_PROPELLER = REGISTRATE
            .block("andesite_propeller", AndesitePropellerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<SmartPropellerBlock> SMART_PROPELLER = REGISTRATE
            .block("smart_propeller", SmartPropellerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<Block> LEVITITE = REGISTRATE
            .block("levitite", p -> new Block(p.mapColor(MapColor.COLOR_PURPLE)))
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<Block> PEARLESCENT_LEVITITE = REGISTRATE
            .block("pearlescent_levitite", p -> new Block(p.mapColor(MapColor.COLOR_PINK)))
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static void init() {}
    public static void register() { init(); }
}
