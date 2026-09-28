package dev.simulated_team.simulated.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Trimmed for Fabric port — full datagen helpers live in NeoForge tree. */
public final class SimBlockStateGen {
    private SimBlockStateGen() {}

    public static <T extends Block> void facingBlockstate(
            final DataGenContext<Block, T> ctx,
            final RegistrateBlockstateProvider prov,
            final String modelPath) {
        prov.directionalBlock(
                ctx.getEntry(),
                blockState -> prov.models().getExistingFile(prov.modLoc(modelPath)));
    }

    public static <I extends BlockItem> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelProvider> coloredBlockItemModel(
            final String texture, final String... folders) {
        return (c, p) -> {
            String path = "block";
            for (final String folder : folders) {
                path += "/" + ("_".equals(folder) ? c.getName() : folder);
            }
            p.withExistingParent(c.getName(), p.modLoc(path)).texture("0", p.modLoc("block/" + texture));
        };
    }
}
