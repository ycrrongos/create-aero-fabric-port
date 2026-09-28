package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Named replacement for the registrate shim's BlockBuilder.
 * Stores the registration name so stress config callbacks can read it.
 */
public class BlockBuilder<T extends Block, P> extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {
    private final AbstractRegistrate<?> owner;
    private final String name;

    public BlockBuilder(AbstractRegistrate<?> owner, String name, Function<BlockBehaviour.Properties, T> factory) {
        this(owner, name, factory, null);
    }

    public BlockBuilder(AbstractRegistrate<?> owner, String name, Function<BlockBehaviour.Properties, T> factory, P parent) {
        this.owner = owner;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public AbstractRegistrate<?> getOwner() {
        return this.owner;
    }

    public BlockBuilder<T, P> initialProperties(Supplier<?> supplier) {
        return this;
    }

    public BlockBuilder<T, P> properties(Function<BlockBehaviour.Properties, BlockBehaviour.Properties> operator) {
        return this;
    }

    public BlockBuilder<T, P> blockstate(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> callback) {
        return this;
    }

    public BlockBuilder<T, P> blockstate(Object callback) {
        return this;
    }

    public BlockBuilder<T, P> loot(NonNullBiConsumer<RegistrateBlockLootTables, ? super T> callback) {
        return this;
    }

    public BlockBuilder<T, P> loot(Object callback) {
        return this;
    }

    public BlockBuilder<T, P> addLayer(Supplier<? extends Supplier<?>> layer) {
        return this;
    }

    public BlockBuilder<T, P> cutoutMipped() {
        return this;
    }

    public BlockBuilder<T, P> cutout() {
        return this;
    }

    public BlockBuilder<T, P> translucent() {
        return this;
    }

    public BlockBuilder<T, P> solid() {
        return this;
    }

    public BlockBuilder<T, P> axeOrPickaxe() {
        return this;
    }

    public BlockBuilder<T, P> pickaxeOnly() {
        return this;
    }

    public BlockBuilder<T, P> axeOnly() {
        return this;
    }

    public BlockBuilder<T, P> simpleItem() {
        return this;
    }

    public BlockBuilder<T, P> item(Object... args) {
        return this;
    }

    public ItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
        return new ItemBuilder<>(this.owner, this.name, properties -> null);
    }

    public <I extends BlockItem> ItemBuilder<I, BlockBuilder<T, P>> item(BiFunction<? super T, Item.Properties, ? extends I> factory) {
        return new ItemBuilder<>(this.owner, this.name, properties -> null);
    }

    public BlockBuilder<T, P> removeTag(Object... tags) {
        return this;
    }

    public BlockBuilder<T, P> tag(Object... tags) {
        return this;
    }

    public BlockBuilder<T, P> model(Object... callbacks) {
        return this;
    }

    public BlockBuilder<T, P> lang(Object... args) {
        return this;
    }

    @Override
    public BlockEntry<T> register() {
        return new BlockEntry<>(Identifier.fromNamespaceAndPath(this.owner == null ? "simulated" : this.owner.getModid(), this.name), null);
    }
}
