package dev.simulated_team.simulated.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/** Helpers bridging CompoundTag-era APIs onto 1.21.11 ValueInput/ValueOutput/CompoundTag codecs. */
public final class ValueIO {
    private ValueIO() {}

    public static void putUUID(final ValueOutput output, final String key, final UUID uuid) {
        output.store(key, UUIDUtil.CODEC, uuid);
    }

    public static void putUUID(final CompoundTag tag, final String key, final UUID uuid) {
        tag.store(key, UUIDUtil.CODEC, uuid);
    }

    public static UUID getUUID(final ValueInput input, final String key) {
        return input.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static UUID getUUID(final CompoundTag tag, final String key) {
        return tag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static boolean hasUUID(final ValueInput input, final String key) {
        return input.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static boolean hasUUID(final CompoundTag tag, final String key) {
        return tag.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static void writeBlockPos(final ValueOutput output, final String key, final BlockPos pos) {
        output.store(key, BlockPos.CODEC, pos);
    }

    public static void writeBlockPos(final CompoundTag tag, final String key, final BlockPos pos) {
        tag.store(key, BlockPos.CODEC, pos);
    }

    public static BlockPos readBlockPos(final ValueInput input, final String key) {
        return input.read(key, BlockPos.CODEC).orElse(BlockPos.ZERO);
    }

    public static BlockPos readBlockPos(final CompoundTag tag, final String key) {
        return tag.read(key, BlockPos.CODEC).orElse(BlockPos.ZERO);
    }

    public static void putCompound(final ValueOutput output, final String key, final CompoundTag nested) {
        output.store(key, CompoundTag.CODEC, nested);
    }

    public static void putCompound(final CompoundTag tag, final String key, final CompoundTag nested) {
        tag.put(key, nested);
    }

    public static CompoundTag getCompoundOrEmpty(final ValueInput input, final String key) {
        return input.read(key, CompoundTag.CODEC).orElseGet(CompoundTag::new);
    }

    public static CompoundTag getCompoundOrEmpty(final CompoundTag tag, final String key) {
        return tag.getCompoundOrEmpty(key);
    }
}
