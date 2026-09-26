package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.api.SimpleResourceManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public class FabricSimpleResourceManagerRegistry implements SimpleResourceManager.Registry {
	@Override
	public void registerListener(final PreparableReloadListener listener) {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener() {
			@Override
			public net.minecraft.resources.Identifier getFabricId() {
				return net.minecraft.resources.Identifier.fromNamespaceAndPath("simulated", "simple_resource_" + System.identityHashCode(listener));
			}
			@Override
			public java.util.concurrent.CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, net.minecraft.server.packs.resources.ResourceManager manager, net.minecraft.util.profiling.ProfilerFiller prepareProfiler, net.minecraft.util.profiling.ProfilerFiller applyProfiler, java.util.concurrent.Executor prepareExecutor, java.util.concurrent.Executor applyExecutor) {
				return listener.reload(barrier, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
			}
		});
	}
}
