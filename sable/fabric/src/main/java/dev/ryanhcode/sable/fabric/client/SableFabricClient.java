package dev.ryanhcode.sable.fabric.client;

import net.fabricmc.loader.api.FabricLoader;
import dev.ryanhcode.sable.fabric.compatibility.flywheel.FlywheelCompat;
import dev.ryanhcode.sable.sublevel.render.dispatcher.SubLevelRenderDispatcher;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.SableClient;
import dev.ryanhcode.sable.SableClientConfig;
import dev.ryanhcode.sable.physics.config.FloatingBlockMaterialDataHandler;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderer;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
public final class SableFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SableClient.init();

        if (FabricLoader.getInstance().isModLoaded("create")) {
            FlywheelCompat.init();
        }

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FloatingBlockMaterialDataHandler.clearMaterials());
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Sable.sablePath("sub_level_renderer");
            }

            @Override
            public @NotNull CompletableFuture<Void> reload(
                    final net.minecraft.server.packs.resources.PreparableReloadListener.SharedState sharedState,
                    final Executor backgroundExecutor,
                    final PreparationBarrier barrier,
                    final Executor gameExecutor
            ) {
                return SubLevelRenderDispatcher.get().reload(sharedState, backgroundExecutor, barrier, gameExecutor);
            }
        });

        ModConfigEvents.loading(Sable.MOD_ID).register(config -> {
            if (config.getSpec().equals(SableClientConfig.SPEC))
                SableClientConfig.onUpdate(false);
        });

        ModConfigEvents.reloading(Sable.MOD_ID).register(config -> {
            if (config.getSpec().equals(SableClientConfig.SPEC))
                SableClientConfig.onUpdate(true);
        });

        ConfigRegistry.INSTANCE.register(Sable.MOD_ID, ModConfig.Type.CLIENT, SableClientConfig.SPEC);
    }
}
