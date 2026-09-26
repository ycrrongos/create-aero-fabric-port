package dev.ryanhcode.sable.render.terrain;

import io.github.ocelot.glslprocessor.api.GlslInjectionPoint;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.GlslNodeList;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.api.node.expression.GlslAssignmentNode;
import io.github.ocelot.glslprocessor.api.node.expression.GlslOperationNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslInvokeFunctionNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslVariableNode;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * GLSL transformations applied to the vanilla terrain shader ({@code minecraft:core/terrain}).
 * <p>
 * Every injected feature is gated by a uniform whose default value (0) keeps the vanilla behavior, because the terrain
 * shader is shared by every terrain pipeline and a freshly linked program starts with all plain uniforms set to 0.
 */
@ApiStatus.Internal
public final class SableTerrainShaderTransformer {

    /**
     * The local variable holding the sky light multiplier, which later transformations may scale further
     */
    public static final String SKY_LIGHT_SCALE_VARIABLE = "sableSkyLightScale";

    private SableTerrainShaderTransformer() {
    }

    private static GlslNodeList main(final GlslTree tree) {
        return tree.mainFunction().orElseThrow(() -> new IllegalStateException("Terrain shader has no main function")).getBody();
    }

    /**
     * Finds the index of the statement declaring the camera relative position {@code pos}.
     */
    private static int findPositionDeclaration(final GlslNodeList body) {
        for (int i = 0; i < body.size(); i++) {
            final String source = body.get(i).toSourceString().trim();
            if (source.startsWith("vec3 pos ") || source.startsWith("vec3 pos=")) {
                return i;
            }
        }
        throw new IllegalStateException("Terrain vertex shader does not declare the camera relative position");
    }

    /**
     * Finds the index of the {@code vertexColor = Color * minecraft_sample_lightmap(...)} statement.
     */
    private static int findLightmapAssignment(final GlslNodeList body) {
        for (int i = 0; i < body.size(); i++) {
            final GlslNode node = body.get(i);
            if (node instanceof final GlslAssignmentNode assignmentNode && assignmentNode.getOperand() == GlslAssignmentNode.Operand.EQUAL) {
                final GlslNode second = assignmentNode.getSecond();
                if (second instanceof final GlslOperationNode operationNode && operationNode.getOperand() == GlslOperationNode.Operand.MULTIPLY) {
                    if (operationNode.getSecond() instanceof final GlslInvokeFunctionNode invokeNode && invokeNode.getHeader() instanceof final GlslVariableNode variableNode && variableNode.getName().equals("minecraft_sample_lightmap")) {
                        return i;
                    }
                }
            }
        }
        throw new IllegalStateException("Terrain vertex shader does not sample the lightmap");
    }

    private static void insertAll(final GlslNodeList body, final int index, final List<GlslNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            body.add(index + i, nodes.get(i));
        }
    }

    /**
     * Adds the sub-level transform and the per sub-level sky light scale to the terrain vertex shader.
     */
    public static void transformVertex(final GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform float %s;".formatted(SableTerrainShader.SUB_LEVEL)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform mat4 %s;".formatted(SableTerrainShader.SUB_LEVEL_TRANSFORM)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform float %s;".formatted(SableTerrainShader.SKY_LIGHT_DIM)));

        final GlslNodeList body = main(tree);

        // Sub-level sections are uploaded in plot space, relative to an anchor near the sub-level.
        // The transform moves them into camera relative world space, so fog and everything after works as usual.
        final int positionIndex = findPositionDeclaration(body);
        insertAll(body, positionIndex + 1, GlslParser.parseExpressionList("""
                if (%s > 0.5) {
                    pos = (%s * vec4(pos, 1.0)).xyz;
                }
                """.formatted(SableTerrainShader.SUB_LEVEL, SableTerrainShader.SUB_LEVEL_TRANSFORM)));

        final int lightmapIndex = findLightmapAssignment(body);
        final List<GlslNode> replacement = GlslParser.parseExpressionList("""
                float %1$s = 1.0 - %2$s;
                vertexColor = Color * minecraft_sample_lightmap(Sampler2, ivec2(vec2(UV2) * vec2(1.0, %1$s)));
                """.formatted(SKY_LIGHT_SCALE_VARIABLE, SableTerrainShader.SKY_LIGHT_DIM));
        body.set(lightmapIndex, replacement.getFirst());
        insertAll(body, lightmapIndex + 1, replacement.subList(1, replacement.size()));
    }

    /**
     * Makes sub-level geometry shade its faces dynamically from its world space normal. The baked shading is removed
     * from sub-level geometry while compiling it.
     */
    public static void transformDynamicShading(final GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform float %s;".formatted(SableTerrainShader.NORMAL_LIGHTING)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform float %s[6];".formatted(SableTerrainShader.BLOCK_FACE_BRIGHTNESS)));
        tree.getBody().addAll(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parse("""
                float sable_block_brightness(vec3 worldNormal) {
                    float darkFromD = pow(clamp(-worldNormal.y, 0.0, 1.0), 3.0) * %1$s[0];
                    float darkFromU = pow(clamp(worldNormal.y, 0.0, 1.0), 3.0) * %1$s[1];
                    float darkFromN = pow(clamp(-worldNormal.z, 0.0, 1.0), 2.0) * %1$s[2];
                    float darkFromS = pow(clamp(worldNormal.z, 0.0, 1.0), 2.0) * %1$s[3];
                    float darkFromW = pow(clamp(-worldNormal.x, 0.0, 1.0), 2.0) * %1$s[4];
                    float darkFromE = pow(clamp(worldNormal.x, 0.0, 1.0), 2.0) * %1$s[5];
                    return darkFromD + darkFromU + darkFromN + darkFromS + darkFromW + darkFromE;
                }
                """.formatted(SableTerrainShader.BLOCK_FACE_BRIGHTNESS)).getBody());

        main(tree).addAll(GlslParser.parseExpressionList("""
                if (%s > 0.5) {
                    vertexColor.rgb *= sable_block_brightness(normalize(mat3(%s) * Normal));
                }
                """.formatted(SableTerrainShader.NORMAL_LIGHTING, SableTerrainShader.SUB_LEVEL_TRANSFORM)));
    }

    /**
     * Darkens world geometry below sub-levels by sampling a top-down depth map of the sub-levels.
     */
    public static void transformSkyLightShadows(final GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform sampler2D %s;".formatted(SableTerrainShader.SHADOW_SAMPLER)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform float %s;".formatted(SableTerrainShader.SHADOW_VOLUME_SIZE)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform float %s;".formatted(SableTerrainShader.SHADOWS_ENABLED)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform vec3 %s;".formatted(SableTerrainShader.SHADOW_ORIGIN)));

        final GlslNodeList body = main(tree);
        final int lightmapIndex = findLightmapAssignment(body);
        insertAll(body, lightmapIndex, GlslParser.parseExpressionList("""
                if (%1$s > 0.0) {
                    float volumeSize = %2$s;
                    vec3 shadowOrigin = %3$s;
                    vec2 shadowUv = ((pos.xz - shadowOrigin.xz) * vec2(1.0, -1.0) + volumeSize) / (volumeSize * 2.0);

                    float sampleAverage = 0.0;
                    int sampleRadius = 3;
                    float spacing = 1.0;

                    for (int i = -sampleRadius; i <= sampleRadius; i++) {
                        for (int j = -sampleRadius; j <= sampleRadius; j++) {
                            float depthSample = texture(%4$s, shadowUv + vec2(i, j) * spacing / (volumeSize * 2.0)).r;
                            float depth = 0.5 + depthSample * (volumeSize - 0.5);
                            float y = shadowOrigin.y - depth;

                            if (y >= pos.y) {
                                float strength = max(min((y - pos.y - 2.0) / 15.0, 1.0), 0.0);
                                float scale = float(i + j) / float(sampleRadius);
                                sampleAverage += max(1.0 - scale, 0.0) * 0.6 * strength;
                            }
                        }
                    }

                    sampleAverage /= float((sampleRadius * 2 + 1) * (sampleRadius * 2 + 1));
                    %5$s *= smoothstep(0.0, 1.0, 1.0 - sampleAverage);
                }
                """.formatted(SableTerrainShader.SHADOWS_ENABLED, SableTerrainShader.SHADOW_VOLUME_SIZE, SableTerrainShader.SHADOW_ORIGIN,
                SableTerrainShader.SHADOW_SAMPLER, SKY_LIGHT_SCALE_VARIABLE)));
    }

    /**
     * Discards translucent terrain fragments inside the water occlusion volumes of sub-levels.
     */
    public static void transformWaterOcclusion(final GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform sampler2D %s;".formatted(SableTerrainShader.WATER_OCCLUSION_CLOSE_SAMPLER)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform sampler2D %s;".formatted(SableTerrainShader.WATER_OCCLUSION_FAR_SAMPLER)));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN, GlslParser.parseExpression("uniform float %s;".formatted(SableTerrainShader.WATER_OCCLUSION_ENABLED)));

        insertAll(main(tree), 0, GlslParser.parseExpressionList("""
                if (%1$s > 0.0) {
                    float closeDepth = texture(%2$s, gl_FragCoord.xy / ScreenSize).r;
                    float farDepth = texture(%3$s, gl_FragCoord.xy / ScreenSize).r;
                    float waterDepth = gl_FragCoord.z;
                    if (waterDepth > closeDepth && waterDepth < farDepth) {
                        discard;
                    }
                }
                """.formatted(SableTerrainShader.WATER_OCCLUSION_ENABLED, SableTerrainShader.WATER_OCCLUSION_CLOSE_SAMPLER, SableTerrainShader.WATER_OCCLUSION_FAR_SAMPLER)));
    }
}
