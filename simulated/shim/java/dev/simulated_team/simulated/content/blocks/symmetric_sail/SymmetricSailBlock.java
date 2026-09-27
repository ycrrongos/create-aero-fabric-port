package dev.simulated_team.simulated.content.blocks.symmetric_sail;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public class SymmetricSailBlock extends Block {
    public SymmetricSailBlock(Properties properties) {
        super(properties);
    }

    public static SymmetricSailBlock withCanvas(Properties properties, DyeColor color) {
        return new SymmetricSailBlock(properties);
    }
}
