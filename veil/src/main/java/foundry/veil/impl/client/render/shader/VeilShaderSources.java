package foundry.veil.impl.client.render.shader;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads Veil GLSL sources and resolves <code>#include namespace:path</code> directives.
 */
@ApiStatus.Internal
public final class VeilShaderSources {

    public static final String PROGRAM_FOLDER = "pinwheel/shaders/program";
    public static final String INCLUDE_FOLDER = "pinwheel/shaders/include";

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^\\s*#\\s*include\\s+[<\"]?([a-z0-9_.\\-]+:[a-z0-9_./\\-]+|[a-z0-9_./\\-]+)[>\"]?\\s*$", Pattern.MULTILINE);
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\s*#\\s*version\\s+(\\d+)(\\s+\\w+)?\\s*$", Pattern.MULTILINE);

    private final Map<Identifier, String> sources;
    private final Map<Identifier, String> includes;

    public VeilShaderSources(Map<Identifier, String> sources, Map<Identifier, String> includes) {
        this.sources = sources;
        this.includes = includes;
    }

    /**
     * Reads every Veil shader source and include from the resource manager.
     */
    public static VeilShaderSources load(ResourceManager resourceManager) {
        Map<Identifier, String> sources = new HashMap<>();
        Map<Identifier, String> includes = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(PROGRAM_FOLDER, id -> !id.getPath().endsWith(".json")).entrySet()) {
            String text = read(entry.getKey(), entry.getValue());
            if (text != null) {
                sources.put(entry.getKey(), text);
            }
        }
        for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(INCLUDE_FOLDER, id -> id.getPath().endsWith(".glsl")).entrySet()) {
            String text = read(entry.getKey(), entry.getValue());
            if (text != null) {
                includes.put(entry.getKey(), text);
            }
        }
        return new VeilShaderSources(sources, includes);
    }

    private static @Nullable String read(Identifier id, Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } catch (IOException e) {
            Veil.LOGGER.error("Failed to read shader source {}", id, e);
            return null;
        }
    }

    /**
     * Gets the raw source of a program stage, e.g. <code>aeronautics:levitite/levitite</code> + <code>.vsh</code>.
     */
    public @Nullable String getSource(Identifier name, String extension) {
        return this.sources.get(name.withPath(PROGRAM_FOLDER + "/" + name.getPath() + extension));
    }

    /**
     * Gets the raw source of an include file, e.g. <code>veil:fog</code>.
     */
    public @Nullable String getInclude(Identifier name) {
        return this.includes.get(name.withPath(INCLUDE_FOLDER + "/" + name.getPath() + ".glsl"));
    }

    /**
     * Inlines all includes of the specified source. Every include is only inserted once.
     *
     * @param source   The source to expand
     * @param included Receives every included file
     */
    public String expandIncludes(String source, Set<Identifier> included) throws IOException {
        Matcher matcher = INCLUDE_PATTERN.matcher(source);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String raw = matcher.group(1);
            Identifier id = raw.contains(":") ? Identifier.parse(raw) : Identifier.withDefaultNamespace(raw);
            String replacement;
            if (included.add(id)) {
                String include = this.getInclude(id);
                if (include == null) {
                    throw new IOException("Unknown shader include: " + id);
                }
                replacement = "// #include " + id + "\n" + stripVersion(this.expandIncludes(include, included)) + "\n";
            } else {
                replacement = "// #include " + id + " (already included)\n";
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    /**
     * Removes any <code>#version</code> directive from the source.
     */
    public static String stripVersion(String source) {
        return VERSION_PATTERN.matcher(source).replaceAll("");
    }

    /**
     * @return The GLSL version declared by the source or <code>-1</code>
     */
    public static int findVersion(String source) {
        Matcher matcher = VERSION_PATTERN.matcher(source);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    /**
     * Assembles the final source of a stage: version, macros, then the expanded body.
     */
    public static String assemble(String expandedBody, Map<String, String> macros, boolean tessellation) {
        int declared = findVersion(expandedBody);
        int version;
        if (declared != -1) {
            version = declared;
        } else if (VeilRenderSystem.glCapabilities().OpenGL41) {
            version = 410;
        } else {
            version = tessellation ? 400 : 330;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("#version ").append(version).append(" core\n");
        if (tessellation && version < 400) {
            builder.append("#extension GL_ARB_tessellation_shader : require\n");
        }
        for (Map.Entry<String, String> macro : macros.entrySet()) {
            builder.append("#define ").append(macro.getKey());
            if (!macro.getValue().isEmpty()) {
                builder.append(' ').append(macro.getValue());
            }
            builder.append('\n');
        }
        builder.append("#line 1\n");
        builder.append(stripVersion(expandedBody));
        return builder.toString();
    }
}
