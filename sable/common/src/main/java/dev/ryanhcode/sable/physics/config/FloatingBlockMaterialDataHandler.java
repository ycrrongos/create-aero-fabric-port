package dev.ryanhcode.sable.physics.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.HashMap;
import java.util.Map;
public class FloatingBlockMaterialDataHandler {
    public static HashMap<Identifier, FloatingBlockMaterial> allMaterials = new HashMap<>();

    public static void addMaterial(final Identifier id, final FloatingBlockMaterial material) {
        allMaterials.put(id, material);
    }

    public static void clearMaterials() {
        allMaterials.clear();
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener<FloatingBlockMaterial> {
        public static final String NAME = "floating_block_material";
        public static final Identifier ID = Sable.sablePath(NAME);

        public static final ReloadListener INSTANCE = new ReloadListener();

        protected ReloadListener() {
            super(FloatingBlockMaterial.CODEC, net.minecraft.resources.FileToIdConverter.json("floating_materials"));
        }

        @Override
        protected void apply(final Map<Identifier, FloatingBlockMaterial> map, final ResourceManager resourceManager, final ProfilerFiller profiler) {
            FloatingBlockMaterialDataHandler.allMaterials.clear();
            for (final Map.Entry<Identifier, FloatingBlockMaterial> entry : map.entrySet()) {
                FloatingBlockMaterialDataHandler.addMaterial(entry.getKey(), entry.getValue());
            }
        }
    }
}
