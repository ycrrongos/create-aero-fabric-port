package foundry.veil.api.client.render.shader.block;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL30C.glBindBufferBase;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;

/**
 * A uniform or shader storage buffer that Veil programs can read from.
 *
 * @param <T> The type of value written into the block
 */
public final class ShaderBlock<T> implements NativeResource {

    private final BufferBinding binding;
    private final BiConsumer<T, ByteBuffer> serializer;
    private long size;
    private int buffer;
    private T value;
    private boolean dirty;

    private ShaderBlock(BufferBinding binding, long size, BiConsumer<T, ByteBuffer> serializer) {
        this.binding = binding;
        this.size = size;
        this.serializer = serializer;
    }

    public static <T> ShaderBlock<T> withSize(BufferBinding binding, long size, BiConsumer<T, ByteBuffer> serializer) {
        return new ShaderBlock<>(binding, size, serializer);
    }

    public void set(T value) {
        this.value = value;
        this.dirty = true;
    }

    public void setSize(long size) {
        this.size = size;
        this.dirty = true;
    }

    /**
     * Uploads the value if needed and binds the block to the specified index.
     */
    public void bind(int index) {
        if (this.buffer == 0) {
            this.buffer = GlStateManager._glGenBuffers();
            this.dirty = true;
        }
        if (this.dirty && this.value != null) {
            ByteBuffer data = MemoryUtil.memCalloc((int) this.size);
            try {
                this.serializer.accept(this.value, data);
                data.rewind();
                GlStateManager._glBindBuffer(this.binding.target, this.buffer);
                glBufferData(this.binding.target, data, GL_DYNAMIC_DRAW);
            } finally {
                MemoryUtil.memFree(data);
            }
            this.dirty = false;
        }
        glBindBufferBase(this.binding.target, index, this.buffer);
    }

    @Override
    public void free() {
        if (this.buffer != 0) {
            GlStateManager._glDeleteBuffers(this.buffer);
            this.buffer = 0;
        }
    }

    public enum BufferBinding {
        UNIFORM(GL_UNIFORM_BUFFER),
        STORAGE(GL_SHADER_STORAGE_BUFFER);

        private final int target;

        BufferBinding(int target) {
            this.target = target;
        }

        public int getTarget() {
            return this.target;
        }
    }
}
