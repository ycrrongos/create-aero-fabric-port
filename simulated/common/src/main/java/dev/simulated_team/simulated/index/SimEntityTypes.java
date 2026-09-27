package dev.simulated_team.simulated.index;

import com.tterrag.registrate.builders.EntityBuilder;
import dev.simulated_team.simulated.service.SimEntityService;
import net.minecraft.world.entity.Entity;

/** Temporary registration surface for Create Fly compile. Full file in Simulated-Project. */
public class SimEntityTypes {
    public record EntityLoaderData(
            int clientTrackingRange,
            int updateFrequency,
            float width,
            float height,
            float eyeHeight,
            boolean sendVelocity,
            boolean immuneToFire,
            boolean fixed
    ) {}

    public static <T extends Entity, P> EntityBuilder<T, P> applyLoaderSpecificTransform(
            final EntityBuilder<T, P> builder, final EntityLoaderData data) {
        return SimEntityService.INSTANCE.loaderEntityTransform(builder, data);
    }

    public static void register() {}
}
