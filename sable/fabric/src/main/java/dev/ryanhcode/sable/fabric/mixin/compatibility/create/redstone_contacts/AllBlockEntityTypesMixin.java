package dev.ryanhcode.sable.fabric.mixin.compatibility.create.redstone_contacts;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.Create;
import dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.redstone_contact.RedstoneContactBlockEntity;
import dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.redstone_contact.RedstoneContactBlockEntityTypeGetter;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Registers the block entity type of redstone contacts ({@code create:redstone_contact}) together with Create's
 * other block entity types.
 * <p>
 * Create Fly does not use Registrate: it registers its block entity types directly into the vanilla registry from the
 * static initializer of {@link AllBlockEntityTypes}. The static initializer of this mixin is merged into it, so the
 * type is registered at the same time, under the same id as upstream. The type is built with Fabric's
 * {@link FabricBlockEntityTypeBuilder} as the vanilla {@code BlockEntityType.BlockEntitySupplier} used by Create Fly's
 * own {@code register} helper is not accessible to Sable.
 */
@Mixin(AllBlockEntityTypes.class)
public class AllBlockEntityTypesMixin implements RedstoneContactBlockEntityTypeGetter {

    @Unique
    private static final BlockEntityType<RedstoneContactBlockEntity> REDSTONE_CONTACT = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Create.MOD_ID, "redstone_contact"),
            FabricBlockEntityTypeBuilder.<RedstoneContactBlockEntity>create(
                    (pos, state) -> new RedstoneContactBlockEntity(AllBlockEntityTypesMixin.REDSTONE_CONTACT, pos, state),
                    AllBlocks.REDSTONE_CONTACT
            ).build()
    );


    @Override
    public BlockEntityType<RedstoneContactBlockEntity> sable$getRedstoneContactType() {
        return REDSTONE_CONTACT;
    }
}
