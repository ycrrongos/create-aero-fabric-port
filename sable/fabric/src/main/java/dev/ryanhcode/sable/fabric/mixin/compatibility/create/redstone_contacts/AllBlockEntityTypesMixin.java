package dev.ryanhcode.sable.fabric.mixin.compatibility.create.redstone_contacts;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.redstone_contact.RedstoneContactBlockEntity;
import dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.redstone_contact.RedstoneContactBlockEntityTypeGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Registers the block entity type of redstone contacts ({@code create:redstone_contact}) together with Create's
 * other block entity types.
 * <p>
 * Create Fly registers its block entity types directly into the vanilla registry from the static initializer of
 * {@link AllBlockEntityTypes} (through {@code AllBlockEntityTypes#register(String, BlockEntityType.BlockEntitySupplier, Block...)}),
 * instead of through Registrate.
 */
@Mixin(AllBlockEntityTypes.class)
public class AllBlockEntityTypesMixin implements RedstoneContactBlockEntityTypeGetter {

    @Shadow
    private static <T extends BlockEntity> BlockEntityType<T> register(final String id, final BlockEntityType.BlockEntitySupplier<T> factory, final Block... blocks) {
        throw new AssertionError();
    }

    @Unique
    private static final BlockEntityType<RedstoneContactBlockEntity> REDSTONE_CONTACT = register(
            "redstone_contact",
            (pos, state) -> new RedstoneContactBlockEntity(AllBlockEntityTypesMixin.REDSTONE_CONTACT, pos, state),
            AllBlocks.REDSTONE_CONTACT
    );


    @Override
    public BlockEntityType<RedstoneContactBlockEntity> sable$getRedstoneContactType() {
        return REDSTONE_CONTACT;
    }
}
