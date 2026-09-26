package foundry.veil.impl.client.render.blocklayer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Geometry of the extra block layers of a single compiled chunk section.
 * <p>
 * The vertex data is copied out of the compile thread buffers and uploaded to the GPU lazily on the render thread.
 */
@ApiStatus.Internal
public final class VeilSectionLayerData implements AutoCloseable {

    private final Map<VeilRenderType, Pending> pending = new IdentityHashMap<>();
    private final Map<VeilRenderType, Uploaded> uploaded = new IdentityHashMap<>();

    public synchronized void add(VeilRenderType layer, ByteBuffer vertices, int vertexCount) {
        ByteBuffer copy = MemoryUtil.memAlloc(vertices.remaining());
        MemoryUtil.memCopy(vertices, copy);
        Pending old = this.pending.put(layer, new Pending(copy, vertexCount));
        if (old != null) {
            MemoryUtil.memFree(old.data());
        }
    }

    public synchronized boolean isEmpty() {
        return this.pending.isEmpty() && this.uploaded.isEmpty();
    }

    /**
     * @return The GPU geometry of the specified layer, uploading it if needed
     */
    public synchronized @Nullable Uploaded get(VeilRenderType layer) {
        Pending pending = this.pending.remove(layer);
        if (pending != null) {
            RenderSystem.assertOnRenderThread();
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Veil section layer " + VeilRenderType.getName(layer), GpuBuffer.USAGE_VERTEX, pending.data());
            MemoryUtil.memFree(pending.data());
            Uploaded old = this.uploaded.put(layer, new Uploaded(buffer, pending.vertexCount()));
            if (old != null) {
                old.buffer().close();
            }
        }
        return this.uploaded.get(layer);
    }

    @Override
    public synchronized void close() {
        this.pending.values().forEach(pending -> MemoryUtil.memFree(pending.data()));
        this.pending.clear();
        this.uploaded.values().forEach(uploaded -> uploaded.buffer().close());
        this.uploaded.clear();
    }

    private record Pending(ByteBuffer data, int vertexCount) {
    }

    public record Uploaded(GpuBuffer buffer, int vertexCount) {
    }
}
