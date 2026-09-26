package foundry.veil.mixin.client;

import foundry.veil.impl.client.render.blocklayer.VeilSectionLayerData;
import foundry.veil.impl.client.render.blocklayer.VeilSectionLayerHolder;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompiledSectionMesh.class)
public class CompiledSectionMeshMixin implements VeilSectionLayerHolder {

    @Unique
    @Nullable
    private volatile VeilSectionLayerData veil$layerData;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void veil$takeLayers(TranslucencyPointOfView translucencyPointOfView, SectionCompiler.Results results, CallbackInfo ci) {
        VeilSectionLayerHolder holder = (VeilSectionLayerHolder) (Object) results;
        this.veil$layerData = holder.veil$getLayerData();
        holder.veil$setLayerData(null);
    }

    @Override
    public @Nullable VeilSectionLayerData veil$getLayerData() {
        return this.veil$layerData;
    }

    @Override
    public void veil$setLayerData(@Nullable VeilSectionLayerData data) {
        this.veil$layerData = data;
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void veil$close(CallbackInfo ci) {
        VeilSectionLayerData data = this.veil$layerData;
        if (data != null) {
            data.close();
            this.veil$layerData = null;
        }
    }
}
