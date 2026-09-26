package dev.ryanhcode.sable.fabric.mixin.compatibility.flywheel;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.fabric.compatibility.flywheel.SableFlywheelShaderOverrides;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Makes Flywheel load Sable's shader overrides ({@code assets/flywheel/flywheel/**} in the Sable jar) instead of the
 * shaders bundled in Create Fly, independent of the order of the mod resource packs.
 * <p>
 * Every shader source, both the ones listed at load and the ones pulled in by {@code #include}, is read through
 * {@code ShaderSources$SourceFinder#readResource}.
 */
@Mixin(targets = "com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources$SourceFinder")
public class ShaderSourcesMixin {

    @Shadow
    @Final
    private ResourceManager manager;

    @ModifyVariable(method = "readResource", at = @At("HEAD"), argsOnly = true)
    private Resource sable$useSableOverride(final Resource resource, @Local(argsOnly = true) final Identifier location) {
        return SableFlywheelShaderOverrides.select(this.manager, location, resource);
    }
}
