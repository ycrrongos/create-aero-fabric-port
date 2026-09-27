package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.foundation.block.DyedBlockList;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Factory includes the block entity type. Create Fly block entities are constructed as
 * (BlockEntityType, BlockPos, BlockState).
 */
public class BlockEntityBuilder<T extends BlockEntity, P> extends AbstractBuilder<BlockEntityType<T>, BlockEntityType<T>, P, BlockEntityBuilder<T, P>> {
    private final AbstractRegistrate<?> owner;
    private final String name;

    public BlockEntityBuilder(AbstractRegistrate<?> owner, String name, BlockEntityFactory<T> factory) {
        this(owner, null, name, null, factory);
    }

    public BlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        this.owner = owner;
        this.name = name;
    }

    public BlockEntityBuilder<T, P> validBlocks(BlockEntry<?>... blocks) {
        return this;
    }

    public BlockEntityBuilder<T, P> validBlock(BlockEntry<?> block) {
        return this;
    }

    public BlockEntityBuilder<T, P> validBlocks(DyedBlockList<?> blocks) {
        return this;
    }

    public BlockEntityBuilder<T, P> renderer(Supplier<RendererFactory> renderer) {
        return this;
    }

    public BlockEntityBuilder<T, P> visual(Supplier<SimpleBlockEntityVisualizer.Factory<T>> visual) {
        return this;
    }

    public BlockEntityBuilder<T, P> visual(Supplier<?> visual, boolean falseIfRenderFast) {
        return this;
    }

    @Override
    public BlockEntityEntry<T> register() {
        String namespace = this.owner == null ? "simulated" : this.owner.getModid();
        return new BlockEntityEntry<>(Identifier.fromNamespaceAndPath(namespace, this.name), null);
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface RendererFactory {
        Object create(BlockEntityRendererProvider.Context context);
    }
}
