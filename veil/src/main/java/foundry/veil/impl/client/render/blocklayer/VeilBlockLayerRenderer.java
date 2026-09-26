package foundry.veil.impl.client.render.blocklayer;

import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.impl.client.render.VeilImmediateRenderer;
import foundry.veil.mixin.client.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fc;

/**
 * Draws extra block layers of chunk sections with the Veil program of the layer.
 */
@ApiStatus.Internal
public final class VeilBlockLayerRenderer {

    private VeilBlockLayerRenderer() {
    }

    /**
     * Renders the specified layer for every visible section of the level.
     */
    public static void renderLevelLayer(VeilRenderType layer, double cameraX, double cameraY, double cameraZ, Matrix4fc modelView, Matrix4fc projection) {
        Iterable<SectionRenderDispatcher.RenderSection> sections = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).veil$getVisibleSections();
        renderSections(layer, sections, cameraX, cameraY, cameraZ, modelView, projection);
    }

    /**
     * Renders the specified layer for the specified sections.
     *
     * @param layer      The layer to render
     * @param sections   The sections to render
     * @param originX    The x origin chunk offsets are relative to (usually the camera)
     * @param originY    The y origin chunk offsets are relative to (usually the camera)
     * @param originZ    The z origin chunk offsets are relative to (usually the camera)
     * @param modelView  The model view matrix
     * @param projection The projection matrix
     */
    public static void renderSections(VeilRenderType layer, Iterable<SectionRenderDispatcher.RenderSection> sections, double originX, double originY, double originZ, Matrix4fc modelView, Matrix4fc projection) {
        ShaderProgram program = layer.setupProgram();
        if (program == null) {
            return;
        }
        program.setDefaultUniforms(modelView, projection);
        ShaderUniformAccess chunkOffset = program.getUniformSafe("ChunkOffset");
        boolean started = false;
        try {
            for (SectionRenderDispatcher.RenderSection section : sections) {
                SectionMesh mesh = section.getSectionMesh();
                if (!(mesh instanceof VeilSectionLayerHolder holder)) {
                    continue;
                }
                VeilSectionLayerData data = holder.veil$getLayerData();
                if (data == null) {
                    continue;
                }
                VeilSectionLayerData.Uploaded uploaded = data.get(layer);
                if (uploaded == null) {
                    continue;
                }
                if (!started) {
                    VeilImmediateRenderer.beginBatch(program, layer.getState());
                    started = true;
                }
                BlockPos origin = section.getRenderOrigin();
                chunkOffset.setVector((float) (origin.getX() - originX), (float) (origin.getY() - originY), (float) (origin.getZ() - originZ));
                VeilImmediateRenderer.drawBuffer(((GlBuffer) uploaded.buffer()).handle, DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, uploaded.vertexCount(), layer.getPatchVertices());
            }
        } finally {
            if (started) {
                VeilImmediateRenderer.endBatch();
            }
            program.clearSamplers();
        }
    }

    /**
     * Draws a single section layer inside a batch. Used by renderers that manage their own sections (e.g. Sable).
     *
     * @return Whether anything was drawn
     */
    public static boolean drawSection(VeilRenderType layer, SectionRenderDispatcher.RenderSection section) {
        SectionMesh mesh = section.getSectionMesh();
        if (!(mesh instanceof VeilSectionLayerHolder holder)) {
            return false;
        }
        VeilSectionLayerData data = holder.veil$getLayerData();
        if (data == null) {
            return false;
        }
        VeilSectionLayerData.Uploaded uploaded = data.get(layer);
        if (uploaded == null) {
            return false;
        }
        RenderSystem.assertOnRenderThread();
        VeilImmediateRenderer.drawBuffer(((GlBuffer) uploaded.buffer()).handle, DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, uploaded.vertexCount(), layer.getPatchVertices());
        return true;
    }

    /**
     * @return Whether the section has geometry in the layer
     */
    public static boolean hasLayer(VeilRenderType layer, SectionRenderDispatcher.RenderSection section) {
        SectionMesh mesh = section.getSectionMesh();
        if (!(mesh instanceof VeilSectionLayerHolder holder)) {
            return false;
        }
        VeilSectionLayerData data = holder.veil$getLayerData();
        return data != null && data.get(layer) != null;
    }
}
