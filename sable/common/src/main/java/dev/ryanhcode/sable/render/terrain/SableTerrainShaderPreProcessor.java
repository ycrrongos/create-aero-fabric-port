package dev.ryanhcode.sable.render.terrain;

import dev.ryanhcode.sable.render.dynamic_shade.SableDynamicDirectionalShading;
import dev.ryanhcode.sable.render.sky_light_shadow.SableSkyLightShadows;
import dev.ryanhcode.sable.render.water_occlusion.WaterOcclusionRenderer;
import foundry.veil.api.client.render.shader.ShaderStage;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import org.jetbrains.annotations.ApiStatus;

/**
 * Applies all of Sable's modifications to the vanilla terrain shader.
 * <ul>
 *     <li>Sub-level transforms and sky light scaling (always, sub-levels can't render without them)</li>
 *     <li>Dynamic directional shading of sub-levels</li>
 *     <li>Sky light shadows cast by sub-levels onto the world</li>
 *     <li>Water occlusion of translucent terrain</li>
 * </ul>
 * The optional features are only compiled in when enabled, toggling them recompiles the vanilla shaders.
 */
@ApiStatus.Internal
public class SableTerrainShaderPreProcessor implements ShaderPreProcessor {

    @Override
    public void modify(final Context ctx, final GlslTree tree) throws GlslSyntaxException {
        if (!(ctx instanceof final MinecraftContext minecraftContext) || !ctx.isSourceFile()) {
            return;
        }

        if (!SableTerrainShader.TERRAIN_SHADER.equals(minecraftContext.shaderId())) {
            return;
        }

        if (ctx.type() == ShaderStage.VERTEX) {
            SableTerrainShaderTransformer.transformVertex(tree);

            if (SableDynamicDirectionalShading.isEnabled()) {
                SableTerrainShaderTransformer.transformDynamicShading(tree);
            }

            if (SableSkyLightShadows.isEnabled()) {
                SableTerrainShaderTransformer.transformSkyLightShadows(tree);
            }
        } else if (ctx.type() == ShaderStage.FRAGMENT) {
            if (WaterOcclusionRenderer.isEnabled()) {
                SableTerrainShaderTransformer.transformWaterOcclusion(tree);
            }
        }
    }
}
