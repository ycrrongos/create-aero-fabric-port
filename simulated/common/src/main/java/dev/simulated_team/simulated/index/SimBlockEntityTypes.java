package dev.simulated_team.simulated.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.absorber.AbsorberBlockEntity;
import dev.simulated_team.simulated.content.blocks.directional_gearshift.DirectionalGearshiftBlockEntity;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.lasers.laser_pointer.LaserPointerBlockEntity;
import dev.simulated_team.simulated.content.blocks.lasers.laser_sensor.LaserSensorBlockEntity;
import dev.simulated_team.simulated.content.blocks.lasers.optical_sensor.OpticalSensorBlockEntity;
import dev.simulated_team.simulated.content.blocks.merging_glue.MergingGlueBlockEntity;
import dev.simulated_team.simulated.content.blocks.nameplate.NameplateBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.directional_receiver.DirectionalLinkedReceiverBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkedReceiverBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.RedstoneMagnetBlockEntity;
import dev.simulated_team.simulated.content.blocks.spring.SpringBlockEntity;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.torsion_spring.TorsionSpringBlockEntity;
import dev.simulated_team.simulated.content.blocks.velocity_sensor.VelocitySensorBlockEntity;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;

/** Slim BE registration matching SimBlocks. */
public class SimBlockEntityTypes {
    private static final SimulatedRegistrate REGISTRATE = Simulated.getRegistrate();

    public static final BlockEntityEntry<SteeringWheelBlockEntity> STEERING_WHEEL = REGISTRATE
            .blockEntity("steering_wheel", SteeringWheelBlockEntity::new)
            .validBlocks(SimBlocks.STEERING_WHEEL)
            .register();

    public static final BlockEntityEntry<SwivelBearingBlockEntity> SWIVEL_BEARING = REGISTRATE
            .blockEntity("swivel_bearing", SwivelBearingBlockEntity::new)
            .validBlocks(SimBlocks.SWIVEL_BEARING)
            .register();

    public static final BlockEntityEntry<TorsionSpringBlockEntity> TORSION_SPRING = REGISTRATE
            .blockEntity("torsion_spring", TorsionSpringBlockEntity::new)
            .validBlocks(SimBlocks.TORSION_SPRING)
            .register();

    public static final BlockEntityEntry<SpringBlockEntity> SPRING = REGISTRATE
            .blockEntity("spring", SpringBlockEntity::new)
            .validBlocks(SimBlocks.SPRING)
            .register();

    public static final BlockEntityEntry<DockingConnectorBlockEntity> DOCKING_CONNECTOR = REGISTRATE
            .blockEntity("docking_connector", DockingConnectorBlockEntity::new)
            .validBlocks(SimBlocks.DOCKING_CONNECTOR)
            .register();

    public static final BlockEntityEntry<DirectionalGearshiftBlockEntity> DIRECTIONAL_GEARSHIFT = REGISTRATE
            .blockEntity("directional_gearshift", DirectionalGearshiftBlockEntity::new)
            .validBlocks(SimBlocks.DIRECTIONAL_GEARSHIFT)
            .register();

    public static final BlockEntityEntry<RedstoneMagnetBlockEntity> REDSTONE_MAGNET = REGISTRATE
            .blockEntity("redstone_magnet", RedstoneMagnetBlockEntity::new)
            .validBlocks(SimBlocks.REDSTONE_MAGNET)
            .register();

    public static final BlockEntityEntry<VelocitySensorBlockEntity> VELOCITY_SENSOR = REGISTRATE
            .blockEntity("velocity_sensor", VelocitySensorBlockEntity::new)
            .validBlocks(SimBlocks.VELOCITY_SENSOR)
            .register();

    public static final BlockEntityEntry<NameplateBlockEntity> NAMEPLATE = REGISTRATE
            .blockEntity("nameplate", NameplateBlockEntity::new)
            .validBlocks(SimBlocks.NAMEPLATE)
            .register();

    public static final BlockEntityEntry<NavTableBlockEntity> NAV_TABLE = REGISTRATE
            .blockEntity("navigation_table", NavTableBlockEntity::new)
            .validBlocks(SimBlocks.NAV_TABLE)
            .register();

    public static final BlockEntityEntry<MergingGlueBlockEntity> MERGING_GLUE = REGISTRATE
            .blockEntity("merging_glue", MergingGlueBlockEntity::new)
            .validBlocks(SimBlocks.MERGING_GLUE)
            .register();

    public static final BlockEntityEntry<AbsorberBlockEntity> ABSORBER = REGISTRATE
            .blockEntity("absorber", AbsorberBlockEntity::new)
            .validBlocks(SimBlocks.ABSORBER)
            .register();

    public static final BlockEntityEntry<LaserPointerBlockEntity> LASER_POINTER = REGISTRATE
            .blockEntity("laser_pointer", LaserPointerBlockEntity::new)
            .validBlocks(SimBlocks.LASER_POINTER)
            .register();

    public static final BlockEntityEntry<LaserSensorBlockEntity> LASER_SENSOR = REGISTRATE
            .blockEntity("laser_sensor", LaserSensorBlockEntity::new)
            .validBlocks(SimBlocks.LASER_SENSOR)
            .register();

    public static final BlockEntityEntry<OpticalSensorBlockEntity> OPTICAL_SENSOR = REGISTRATE
            .blockEntity("optical_sensor", OpticalSensorBlockEntity::new)
            .validBlocks(SimBlocks.OPTICAL_SENSOR)
            .register();

    public static final BlockEntityEntry<DirectionalLinkedReceiverBlockEntity> DIRECTIONAL_LINKED_RECEIVER = REGISTRATE
            .blockEntity("directional_linked_receiver", DirectionalLinkedReceiverBlockEntity::new)
            .validBlocks(SimBlocks.DIRECTIONAL_LINKED_RECEIVER)
            .register();

    public static final BlockEntityEntry<ModulatingLinkedReceiverBlockEntity> MODULATING_LINKED_RECEIVER = REGISTRATE
            .blockEntity("modulating_linked_receiver", ModulatingLinkedReceiverBlockEntity::new)
            .validBlocks(SimBlocks.MODULATING_LINKED_RECEIVER)
            .register();

    public static final BlockEntityEntry<LinkedTypewriterBlockEntity> LINKED_TYPEWRITER = REGISTRATE
            .blockEntity("linked_typewriter", LinkedTypewriterBlockEntity::new)
            .validBlocks(SimBlocks.LINKED_TYPEWRITER)
            .register();

    public static void register() {}
}
