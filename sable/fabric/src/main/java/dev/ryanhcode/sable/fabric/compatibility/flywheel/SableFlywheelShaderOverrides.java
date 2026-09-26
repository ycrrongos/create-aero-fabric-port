package dev.ryanhcode.sable.fabric.compatibility.flywheel;

import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;
import dev.ryanhcode.sable.Sable;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

/**
 * Sable overrides some of Flywheel's internal shaders (see {@code assets/flywheel/explanation.md}).
 * <p>
 * On NeoForge the override is done by loading Sable's resources after Flywheel's. On Fabric, Flywheel is bundled in
 * Create Fly (mod id {@code create}), and the order of mod resource packs is not something we should rely on, so the
 * shader source loader explicitly prefers Sable's copy of a shader over Create Fly's.
 */
public final class SableFlywheelShaderOverrides {

    /**
     * The id of the resource pack Create Fly's bundled Flywheel shaders are loaded from
     */
    public static final String FLYWHEEL_PROVIDER_PACK_ID = "create";

    private SableFlywheelShaderOverrides() {
    }

    /**
     * Picks the resource Flywheel should read a shader source from.
     *
     * @param manager  The resource manager the shader sources are loaded with
     * @param location The shader location, without the {@link ShaderSources#SHADER_DIR} prefix
     * @param resource The resource Flywheel resolved for the location
     * @return Sable's override of the shader if the resolved resource is Create Fly's copy of it, otherwise the resolved resource
     */
    public static Resource select(final ResourceManager manager, final Identifier location, final Resource resource) {
        // Only replace Create Fly's own copy, resource packs placed above it are respected
        if (!FLYWHEEL_PROVIDER_PACK_ID.equals(resource.sourcePackId())) {
            return resource;
        }

        final List<Resource> stack = manager.getResourceStack(location.withPrefix(ShaderSources.SHADER_DIR));

        for (int i = stack.size() - 1; i >= 0; i--) {
            final Resource candidate = stack.get(i);

            if (Sable.MOD_ID.equals(candidate.sourcePackId())) {
                return candidate;
            }
        }

        return resource;
    }
}
