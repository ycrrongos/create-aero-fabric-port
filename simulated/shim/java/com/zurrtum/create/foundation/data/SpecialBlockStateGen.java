package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.modelgen.ModelFile;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** The helpers jar's copy of this class has no methods. Subclasses call generate/getModel. */
public abstract class SpecialBlockStateGen {
    public <T extends Block> void generate(DataGenContext<Block, T> context, RegistrateBlockstateProvider provider) {}

    protected abstract int getXRotation(BlockState state);

    protected abstract int getYRotation(BlockState state);

    public abstract <T extends Block> ModelFile getModel(DataGenContext<Block, T> context, RegistrateBlockstateProvider provider, BlockState state);

    protected int horizontalAngle(Direction direction) {
        return (int) direction.toYRot();
    }
}
