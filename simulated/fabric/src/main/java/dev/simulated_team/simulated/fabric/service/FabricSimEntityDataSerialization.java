package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimEntityDataSerialization;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/** Fabric has no global EntityDataSerializer registry like NeoForge — keep local map for now. */
public class FabricSimEntityDataSerialization implements SimEntityDataSerialization {
	public static final Map<Identifier, EntityDataSerializer<?>> SERIALIZERS = new HashMap<>();

	@Override
	public <A, T extends EntityDataSerializer<A>> void registerDataSerializer(final String name, final T serializer) {
		SERIALIZERS.put(Identifier.fromNamespaceAndPath("simulated", name), serializer);
	}
}
