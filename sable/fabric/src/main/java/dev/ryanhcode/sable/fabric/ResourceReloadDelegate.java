package dev.ryanhcode.sable.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
public class ResourceReloadDelegate implements IdentifiableResourceReloadListener {
    private final Identifier id;
    private final SimpleJsonResourceReloadListener<?> delegate;

    public ResourceReloadDelegate(final Identifier id, final SimpleJsonResourceReloadListener<?> delegate) {
        this.id = id;
        this.delegate = delegate;
    }

    @Override
    public Identifier getFabricId() {
        return this.id;
    }

    @Override
    public CompletableFuture<Void> reload(
            final PreparableReloadListener.SharedState sharedState,
            final Executor backgroundExecutor,
            final PreparableReloadListener.PreparationBarrier barrier,
            final Executor gameExecutor
    ) {
        return this.delegate.reload(sharedState, backgroundExecutor, barrier, gameExecutor);
    }
}
