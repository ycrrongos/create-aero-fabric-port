package dev.simulated_team.simulated.index;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.zurrtum.create.foundation.data.SharedProperties;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.absorber.AbsorberBlock;
import dev.simulated_team.simulated.content.blocks.directional_gearshift.DirectionalGearshiftBlock;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlock;
import dev.simulated_team.simulated.content.blocks.lasers.laser_pointer.LaserPointerBlock;
import dev.simulated_team.simulated.content.blocks.lasers.laser_sensor.LaserSensorBlock;
import dev.simulated_team.simulated.content.blocks.lasers.optical_sensor.OpticalSensorBlock;
import dev.simulated_team.simulated.content.blocks.merging_glue.MergingGlueBlock;
import dev.simulated_team.simulated.content.blocks.nameplate.NameplateBlock;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlock;
import dev.simulated_team.simulated.content.blocks.redstone.directional_receiver.DirectionalLinkedReceiverBlock;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlock;
import dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkedReceiverBlock;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.RedstoneMagnetBlock;
import dev.simulated_team.simulated.content.blocks.spring.SpringBlock;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelBlock;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlock;
import dev.simulated_team.simulated.content.blocks.symmetric_sail.SymmetricSailBlock;
import dev.simulated_team.simulated.content.blocks.torsion_spring.TorsionSpringBlock;
import dev.simulated_team.simulated.content.blocks.velocity_sensor.VelocitySensorBlock;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;

/**
 * Slim Create Fly registration for blocks that already compile.
 * Full NeoForge SimBlocks lives in Simulated-Project / excluded-wip copies.
 */
public class SimBlocks {
    private static final SimulatedRegistrate REGISTRATE = Simulated.getRegistrate();

    public static final BlockEntry<SteeringWheelBlock> STEERING_WHEEL = REGISTRATE
            .block("steering_wheel", SteeringWheelBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<SwivelBearingBlock> SWIVEL_BEARING = REGISTRATE
            .block("swivel_bearing", SwivelBearingBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<TorsionSpringBlock> TORSION_SPRING = REGISTRATE
            .block("torsion_spring", TorsionSpringBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<SpringBlock> SPRING = REGISTRATE
            .block("spring", SpringBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<DockingConnectorBlock> DOCKING_CONNECTOR = REGISTRATE
            .block("docking_connector", DockingConnectorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<DirectionalGearshiftBlock> DIRECTIONAL_GEARSHIFT = REGISTRATE
            .block("directional_gearshift", DirectionalGearshiftBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<RedstoneMagnetBlock> REDSTONE_MAGNET = REGISTRATE
            .block("redstone_magnet", RedstoneMagnetBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<VelocitySensorBlock> VELOCITY_SENSOR = REGISTRATE
            .block("velocity_sensor", VelocitySensorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<NameplateBlock> NAMEPLATE = REGISTRATE
            .block("nameplate", NameplateBlock::new)
            .initialProperties(SharedProperties::wooden)
            .item().build().register();

    public static final BlockEntry<NavTableBlock> NAV_TABLE = REGISTRATE
            .block("navigation_table", NavTableBlock::new)
            .initialProperties(SharedProperties::wooden)
            .item().build().register();

    public static final BlockEntry<MergingGlueBlock> MERGING_GLUE = REGISTRATE
            .block("merging_glue", MergingGlueBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<AbsorberBlock> ABSORBER = REGISTRATE
            .block("absorber", AbsorberBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<LaserPointerBlock> LASER_POINTER = REGISTRATE
            .block("laser_pointer", LaserPointerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<LaserSensorBlock> LASER_SENSOR = REGISTRATE
            .block("laser_sensor", LaserSensorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<OpticalSensorBlock> OPTICAL_SENSOR = REGISTRATE
            .block("optical_sensor", OpticalSensorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<DirectionalLinkedReceiverBlock> DIRECTIONAL_LINKED_RECEIVER = REGISTRATE
            .block("directional_linked_receiver", DirectionalLinkedReceiverBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<ModulatingLinkedReceiverBlock> MODULATING_LINKED_RECEIVER = REGISTRATE
            .block("modulating_linked_receiver", ModulatingLinkedReceiverBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<LinkedTypewriterBlock> LINKED_TYPEWRITER = REGISTRATE
            .block("linked_typewriter", LinkedTypewriterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .item().build().register();

    public static final BlockEntry<SymmetricSailBlock> WHITE_SAIL = REGISTRATE
            .block("white_sail", SymmetricSailBlock::new)
            .initialProperties(SharedProperties::wooden)
            .item().build().register();

    public static void register() {}
}
