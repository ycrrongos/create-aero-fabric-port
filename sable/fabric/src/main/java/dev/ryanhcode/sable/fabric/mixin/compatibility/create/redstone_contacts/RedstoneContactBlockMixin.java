package dev.ryanhcode.sable.fabric.mixin.compatibility.create.redstone_contacts;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.redstone.contact.RedstoneContactBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.redstone_contact.RedstoneContactBlockEntity;
import dev.ryanhcode.sable.fabric.mixinhelper.compatibility.create.redstone_contact.RedstoneContactBlockEntityTypeGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RedstoneContactBlock.class)
public class RedstoneContactBlockMixin extends WrenchableDirectionalBlock implements IBE<RedstoneContactBlockEntity> {

    /**
     * Created lazily: Create Fly's {@link AllBlockEntityTypes} eagerly reads the blocks of {@code AllBlocks} in its
     * static initializer, so it must not be initialized while {@code AllBlocks} is still creating this block.
     */
    @Unique
    private static AllBlockEntityTypes sable$cursed;

    public RedstoneContactBlockMixin(final Properties properties) {
        super(properties);
    }

    @Override
    public Class<RedstoneContactBlockEntity> getBlockEntityClass() {
        return RedstoneContactBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstoneContactBlockEntity> getBlockEntityType() {
        if (sable$cursed == null) {
            sable$cursed = new AllBlockEntityTypes();
        }

        return ((RedstoneContactBlockEntityTypeGetter) sable$cursed).sable$getRedstoneContactType();
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(final Level level, final BlockState p_153213_, final BlockEntityType<S> p_153214_) {
        if (!level.isClientSide()) {
            return IBE.super.getTicker(level, p_153213_, p_153214_);
        } else {
            return null;
        }
    }
}
