package dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.redstone_contact;

import net.minecraft.world.level.block.entity.BlockEntityType;

public interface RedstoneContactBlockEntityTypeGetter {

    BlockEntityType<RedstoneContactBlockEntity> sable$getRedstoneContactType();

}
