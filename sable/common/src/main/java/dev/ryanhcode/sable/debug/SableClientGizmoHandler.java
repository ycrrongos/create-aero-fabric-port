package dev.ryanhcode.sable.debug;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

/**
 * Debug gizmo handler stub for 1.21.11 (full RenderPipeline/debug draw port deferred).
 */
public class SableClientGizmoHandler {

    private Vec3 mouseDir = Vec3.ZERO;
    private boolean enabled = false;
    private @Nullable GizmoSelection selection;

    public void init() {
        // no-op until debug render is ported
    }

    public static Vec3 getRay(final Matrix4fc projectionMatrix, final float normalizedMouseX, final float normalizedMouseY) {
        return Vec3.ZERO;
    }

    public @Nullable GizmoSelection getSelection() {
        return this.selection;
    }

    public Vec3 getMouseDir() {
        return this.mouseDir;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void start() {
        this.enabled = true;
    }

    public void stop() {
        this.enabled = false;
        this.selection = null;
    }
}
