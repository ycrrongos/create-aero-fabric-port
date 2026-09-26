package foundry.veil.impl.client.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import foundry.veil.api.client.render.VeilRenderSystem;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Remembers which projection matrix was written into each vanilla projection uniform buffer, so the matrix vanilla is
 * currently rendering with is always known on the CPU.
 */
@ApiStatus.Internal
public final class VeilProjectionTracker {

    private static final Map<GpuBufferSlice, Matrix4f> SLICES = new WeakHashMap<>();
    private static final Matrix4f SAVED = new Matrix4f();

    private VeilProjectionTracker() {
    }

    public static void record(GpuBufferSlice slice, Matrix4fc matrix) {
        Matrix4f stored = SLICES.computeIfAbsent(slice, unused -> new Matrix4f());
        stored.set(matrix);
    }

    public static void onSetProjection(GpuBufferSlice slice) {
        Matrix4f matrix = SLICES.get(slice);
        if (matrix != null) {
            VeilRenderSystem.setProjectionMatrix(matrix);
        }
    }

    public static void onBackup() {
        SAVED.set(VeilRenderSystem.getProjectionMatrix());
    }

    public static void onRestore() {
        VeilRenderSystem.setProjectionMatrix(SAVED);
    }
}
