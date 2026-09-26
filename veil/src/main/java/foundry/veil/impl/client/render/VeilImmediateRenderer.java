package foundry.veil.impl.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import foundry.veil.api.client.render.VeilDrawState;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL40C.GL_PATCHES;
import static org.lwjgl.opengl.GL40C.GL_PATCH_VERTICES;
import static org.lwjgl.opengl.GL40C.glPatchParameteri;

/**
 * Draws mesh data with a Veil program using raw OpenGL, outside of vanilla render passes.
 */
@ApiStatus.Internal
public final class VeilImmediateRenderer {

    private static final Map<VertexFormat, Integer> VERTEX_ARRAYS = new HashMap<>();
    private static int vertexBuffer;
    private static int indexBuffer;
    private static int emptyVertexArray;

    private VeilImmediateRenderer() {
    }

    private static int getVertexArray(VertexFormat format) {
        Integer existing = VERTEX_ARRAYS.get(format);
        if (existing != null) {
            GlStateManager._glBindVertexArray(existing);
            GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
            setupAttributes(format, false);
            return existing;
        }
        int vao = GlStateManager._glGenVertexArrays();
        GlStateManager._glBindVertexArray(vao);
        GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        setupAttributes(format, true);
        VERTEX_ARRAYS.put(format, vao);
        return vao;
    }

    private static void setupAttributes(VertexFormat format, boolean enable) {
        int stride = format.getVertexSize();
        List<VertexFormatElement> elements = format.getElements();
        for (int i = 0; i < elements.size(); i++) {
            VertexFormatElement element = elements.get(i);
            if (enable) {
                GlStateManager._enableVertexAttribArray(i);
            }
            long offset = format.getOffset(element);
            switch (element.usage()) {
                case POSITION, GENERIC, UV -> {
                    if (element.type() == VertexFormatElement.Type.FLOAT) {
                        GlStateManager._vertexAttribPointer(i, element.count(), GlConst.toGl(element.type()), false, stride, offset);
                    } else {
                        GlStateManager._vertexAttribIPointer(i, element.count(), GlConst.toGl(element.type()), stride, offset);
                    }
                }
                case NORMAL, COLOR -> GlStateManager._vertexAttribPointer(i, element.count(), GlConst.toGl(element.type()), true, stride, offset);
            }
        }
    }

    private static void ensureBuffers() {
        if (vertexBuffer == 0) {
            vertexBuffer = GlStateManager._glGenBuffers();
            indexBuffer = GlStateManager._glGenBuffers();
            emptyVertexArray = GlStateManager._glGenVertexArrays();
        }
    }

    /**
     * Draws the specified mesh with the program into the currently bound Veil framebuffer (or the main target).
     *
     * @param meshData      The mesh to draw. It is closed by this method
     * @param program       The program to draw with
     * @param state         The fixed-function state to use
     * @param patchVertices The number of vertices per tessellation patch, or <code>0</code> to use the mesh primitive mode
     */
    public static void draw(MeshData meshData, ShaderProgram program, VeilDrawState state, int patchVertices) {
        @Nullable AdvancedFbo bound = AdvancedFbo.getBound();
        AdvancedFbo target = bound != null ? bound : AdvancedFbo.getMainFramebuffer();
        drawToFramebuffer(meshData, program, state, patchVertices, target.getId(), target.getWidth(), target.getHeight());
    }

    /**
     * Draws the specified mesh into the specified raw OpenGL framebuffer.
     */
    public static void drawToFramebuffer(MeshData meshData, ShaderProgram program, VeilDrawState state, int patchVertices, int framebuffer, int width, int height) {
        try (meshData) {
            RenderSystem.assertOnRenderThread();
            ensureBuffers();
            MeshData.DrawState drawState = meshData.drawState();
            if (drawState.vertexCount() == 0) {
                return;
            }

            GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
            GlStateManager._viewport(0, 0, width, height);
            state.apply();
            program.bind();

            ByteBuffer vertices = meshData.vertexBuffer();
            GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
            glBufferData(GL_ARRAY_BUFFER, vertices.remaining(), GL_STREAM_DRAW);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            getVertexArray(drawState.format());

            if (patchVertices > 0 && program.hasTessellation()) {
                glPatchParameteri(GL_PATCH_VERTICES, patchVertices);
                GlStateManager._drawArrays(GL_PATCHES, 0, drawState.vertexCount());
            } else {
                ByteBuffer indices = meshData.indexBuffer();
                int mode = GlConst.toGl(drawState.mode());
                if (indices != null) {
                    GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.remaining(), GL_STREAM_DRAW);
                    glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indices);
                    GlStateManager._drawElements(mode, drawState.indexCount(), GlConst.toGl(drawState.indexType()), 0L);
                } else if (drawState.mode() == VertexFormat.Mode.QUADS || drawState.mode() == VertexFormat.Mode.LINES) {
                    RenderSystem.AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer(drawState.mode());
                    GpuBuffer buffer = sequential.getBuffer(drawState.indexCount());
                    GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ((GlBuffer) buffer).handle);
                    GlStateManager._drawElements(mode, drawState.indexCount(), GlConst.toGl(sequential.type()), 0L);
                } else {
                    GlStateManager._drawArrays(mode, 0, drawState.vertexCount());
                }
            }
        } finally {
            finish();
        }
    }

    /**
     * Draws a full screen triangle with the specified program. Used by post-processing.
     */
    public static void drawScreenQuad(ShaderProgram program, VeilDrawState state) {
        RenderSystem.assertOnRenderThread();
        ensureBuffers();
        try {
            bindTarget();
            state.apply();
            program.bind();
            GlStateManager._glBindVertexArray(emptyVertexArray);
            GlStateManager._drawArrays(GL_TRIANGLES, 0, 3);
        } finally {
            finish();
        }
    }

    /**
     * Starts drawing several vertex buffers with the same program into the currently bound Veil framebuffer.
     */
    public static void beginBatch(ShaderProgram program, VeilDrawState state) {
        RenderSystem.assertOnRenderThread();
        ensureBuffers();
        bindTarget();
        state.apply();
        program.bind();
    }

    /**
     * Draws a vertex buffer inside a batch started with {@link #beginBatch(ShaderProgram, VeilDrawState)}.
     *
     * @param buffer        The OpenGL vertex buffer
     * @param format        The format of the vertices
     * @param mode          The primitive mode of the vertices
     * @param vertexCount   The number of vertices to draw
     * @param patchVertices The number of vertices per tessellation patch or <code>0</code>
     */
    public static void drawBuffer(int buffer, VertexFormat format, VertexFormat.Mode mode, int vertexCount, int patchVertices) {
        if (vertexCount <= 0) {
            return;
        }
        Integer vao = VERTEX_ARRAYS.get(format);
        if (vao == null) {
            getVertexArray(format);
            vao = VERTEX_ARRAYS.get(format);
        }
        GlStateManager._glBindVertexArray(vao);
        GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, buffer);
        setupAttributes(format, false);
        if (patchVertices > 0) {
            glPatchParameteri(GL_PATCH_VERTICES, patchVertices);
            GlStateManager._drawArrays(GL_PATCHES, 0, vertexCount);
        } else if (mode == VertexFormat.Mode.QUADS || mode == VertexFormat.Mode.LINES) {
            int indexCount = mode.indexCount(vertexCount);
            RenderSystem.AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer(mode);
            GpuBuffer indices = sequential.getBuffer(indexCount);
            GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ((GlBuffer) indices).handle);
            GlStateManager._drawElements(GlConst.toGl(mode), indexCount, GlConst.toGl(sequential.type()), 0L);
        } else {
            GlStateManager._drawArrays(GlConst.toGl(mode), 0, vertexCount);
        }
    }

    /**
     * Ends a batch started with {@link #beginBatch(ShaderProgram, VeilDrawState)}.
     */
    public static void endBatch() {
        finish();
    }

    private static void bindTarget() {
        @Nullable AdvancedFbo bound = AdvancedFbo.getBound();
        AdvancedFbo target = bound != null ? bound : AdvancedFbo.getMainFramebuffer();
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, target.getId());
        GlStateManager._viewport(0, 0, target.getWidth(), target.getHeight());
    }

    private static void finish() {
        GlStateManager._glBindVertexArray(0);
        GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        VeilGlStateSync.useProgram(0);
        VeilGlStateSync.restoreNonPipelineState();
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}
