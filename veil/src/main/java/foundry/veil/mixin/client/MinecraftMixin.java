package foundry.veil.mixin.client;

import foundry.veil.VeilClient;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V", shift = At.Shift.BEFORE))
    private void veil$init(CallbackInfo ci) {
        VeilClient.initRenderer((Minecraft) (Object) this, this.resourceManager);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void veil$close(CallbackInfo ci) {
        VeilClient.close();
    }
}
