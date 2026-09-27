package dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability;


import dev.simulated_team.simulated.util.ValueIO;
import net.minecraft.nbt.CompoundTag;
import org.joml.Vector3d;
import java.util.UUID;
public record LodestoneInformation(UUID id, Vector3d projectedPos) {
	public CompoundTag saveAsCompound() {
		final CompoundTag data = new CompoundTag();
		ValueIO.putUUID(data, "trackerID", this.id);

		return data;
	}

	public static LodestoneInformation loadFromCompound(final CompoundTag tag) {
		return new LodestoneInformation(ValueIO.getUUID(tag, "trackerID"), new Vector3d());
	}
}
