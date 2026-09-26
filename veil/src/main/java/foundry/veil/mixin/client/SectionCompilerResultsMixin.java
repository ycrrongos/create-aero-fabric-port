package foundry.veil.mixin.client;

import foundry.veil.impl.client.render.blocklayer.VeilSectionLayerData;
import foundry.veil.impl.client.render.blocklayer.VeilSectionLayerHolder;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionCompiler.Results.class)
public class SectionCompilerResultsMixin implements VeilSectionLayerHolder {

    @Unique
    @Nullable
    private VeilSectionLayerData veil$layerData;

    @Override
    public @Nullable VeilSectionLayerData veil$getLayerData() {
        return this.veil$layerData;
    }

    @Override
    public void veil$setLayerData(@Nullable VeilSectionLayerData data) {
        this.veil$layerData = data;
    }

    @Inject(method = "release", at = @At("TAIL"))
    private void veil$release(CallbackInfo ci) {
        // Ownership is transferred to the compiled mesh when one is created, only free abandoned results
        if (this.veil$layerData != null) {
            this.veil$layerData.close();
            this.veil$layerData = null;
        }
    }
}
