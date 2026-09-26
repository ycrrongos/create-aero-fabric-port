package dev.ryanhcode.sable.fabric.mixin.compatibility.create.stock_ticker;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.equipment.hats.HatFeatureRenderer;
import com.zurrtum.create.client.content.equipment.hats.HatState;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlock;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Makes the logistics hat of entities seated next to a stock ticker look for the stock ticker around the seat, when the
 * seat is on a sub-level.
 * <p>
 * Create Fly no longer has {@code EntityHats#getLogisticsHatFor}; the hat is picked while extracting the render state,
 * in the {@code addHat} handler of its own {@code com.zurrtum.create.client.mixin.LivingEntityRendererMixin}, which
 * searches around {@link LivingEntity#blockPosition()}. As that handler is merged into {@link LivingEntityRenderer}
 * under a generated name, this mixin runs after it (higher priority) and redoes the logistics hat lookup from the block
 * position of the root vehicle, which is what the upstream redirect of {@code blockPosition()} did.
 */
@Mixin(value = LivingEntityRenderer.class, priority = 1100)
public class EntityHatsMixin {

    @Shadow
    @Final
    protected List<RenderLayer<?, ?>> layers;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void sable$getStockTickerPosition(final LivingEntity entity, final LivingEntityRenderState renderState, final float partialTick, final CallbackInfo ci) {
        if (!entity.isPassenger() || !(entity.getVehicle() instanceof SeatEntity)) {
            return;
        }

        final Entity vehicle = entity.getRootVehicle();

        if (Sable.HELPER.getContaining(vehicle) == null) {
            return;
        }

        if (this.layers.stream().noneMatch(layer -> layer instanceof HatFeatureRenderer)) {
            return;
        }

        final Level level = entity.level();
        final BlockPos pos = vehicle.blockPosition();
        int stations = 0;

        for (final Direction d : Iterate.horizontalDirections) {
            for (final int y : Iterate.zeroAndOne) {
                if (level.getBlockState(pos.relative(d).above(y)).getBlock() instanceof StockTickerBlock) {
                    stations++;
                }
            }
        }

        final HatState state = (HatState) renderState;

        if (stations == 1) {
            state.create$setHat(AllPartialModels.LOGISTICS_HAT);
            state.create$updateHatInfo(entity);
        } else {
            state.create$setHat(null);
        }
    }
}
