package dev.ryanhcode.sable.fabric.mixin.compatibility.create.contraptions;

import com.zurrtum.create.client.content.contraptions.actors.contraptionControls.ContraptionControlsMovementRender;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes the rendering for the Contraption Controls to take sublevels into account
 * <p>
 * Create Fly moved {@code ContraptionControlsRenderer#renderInContraption} to
 * {@link ContraptionControlsMovementRender#getRenderState}, which measures the distance between the camera and the
 * contraption controls to decide whether their text is drawn.
 * The model transform of the contraption is already applied to the pose the render state is drawn with by
 * {@code ContraptionEntityRenderer#submit}, so the upstream {@code getViewProjection -> getModelViewProjection} redirect
 * has no equivalent left to fix.
 */
@Mixin(ContraptionControlsMovementRender.class)
public class ContraptionControlsRendererMixin {
    @Redirect(method = "getRenderState",
            at = @At(value = "FIELD", target = "Lcom/zurrtum/create/content/contraptions/behaviour/MovementContext;position:Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
    private Vec3 sable$distanceRemix(final MovementContext instance) {
        if (instance.position == null) {
            return null;
        }

        return Sable.HELPER.projectOutOfSubLevel(instance.world, instance.position);
    }
}
