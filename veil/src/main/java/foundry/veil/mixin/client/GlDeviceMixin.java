package foundry.veil.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import foundry.veil.impl.client.render.shader.VanillaShaderProcessor;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlDevice.class)
public class GlDeviceMixin {

    @WrapOperation(method = "compileShader", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/ShaderSource;get(Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/shaders/ShaderType;)Ljava/lang/String;"))
    private String veil$captureShader(ShaderSource instance, Identifier id, ShaderType type, Operation<String> original) {
        VanillaShaderProcessor.setCurrent(id, type);
        return original.call(instance, id, type);
    }

    @WrapOperation(method = "compileShader", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;injectDefines(Ljava/lang/String;Lnet/minecraft/client/renderer/ShaderDefines;)Ljava/lang/String;"))
    private String veil$processShader(String source, ShaderDefines defines, Operation<String> original) {
        return VanillaShaderProcessor.process(original.call(source, defines), defines);
    }
}
