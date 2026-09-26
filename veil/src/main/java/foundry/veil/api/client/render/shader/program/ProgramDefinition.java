package foundry.veil.api.client.render.shader.program;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import foundry.veil.api.client.render.shader.ShaderPreDefinitions;
import foundry.veil.api.client.render.shader.ShaderStage;
import foundry.veil.api.client.render.shader.texture.ShaderTextureSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The JSON definition of a Veil program, loaded from <code>pinwheel/shaders/program/&lt;name&gt;.json</code>.
 *
 * @param stages             The source of every stage of the program
 * @param definitions        Definitions to add to every stage
 * @param definitionDefaults Default values for definitions
 * @param textures           Textures bound to samplers by name
 */
public record ProgramDefinition(EnumMap<ShaderStage, Identifier> stages,
                                String[] definitions,
                                Map<String, String> definitionDefaults,
                                Map<String, ShaderTextureSource> textures) {

    public @Nullable Identifier vertex() {
        return this.stages.get(ShaderStage.VERTEX);
    }

    public @Nullable Identifier fragment() {
        return this.stages.get(ShaderStage.FRAGMENT);
    }

    public boolean hasTessellation() {
        return this.stages.containsKey(ShaderStage.TESS_CONTROL) || this.stages.containsKey(ShaderStage.TESS_EVALUATION);
    }

    /**
     * Computes the macros every stage of this program is compiled with.
     *
     * @param dependencies Receives all definition names this program depends on
     * @param definitions  The global shader definitions
     */
    public Map<String, String> getMacros(Set<String> dependencies, ShaderPreDefinitions definitions) {
        Map<String, String> macros = new LinkedHashMap<>(definitions.getStaticDefinitions());
        for (String name : this.definitions) {
            String definition = definitions.getDefinition(name);
            if (definition != null) {
                macros.put(name.toUpperCase(Locale.ROOT), definition);
            } else {
                macros.put(name.toUpperCase(Locale.ROOT), this.definitionDefaults.getOrDefault(name, "1"));
            }
            dependencies.add(name);
        }
        return macros;
    }

    public static ProgramDefinition parse(JsonObject json) throws JsonParseException {
        EnumMap<ShaderStage, Identifier> stages = new EnumMap<>(ShaderStage.class);
        for (ShaderStage stage : ShaderStage.values()) {
            if (json.has(stage.getJsonKey())) {
                stages.put(stage, Identifier.parse(GsonHelper.getAsString(json, stage.getJsonKey())));
            }
        }

        List<String> definitions = new ArrayList<>();
        Map<String, String> defaults = new HashMap<>();
        if (json.has("definitions")) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "definitions");
            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                if (element.isJsonPrimitive()) {
                    definitions.add(element.getAsString());
                } else if (element.isJsonObject()) {
                    Set<Map.Entry<String, JsonElement>> entries = element.getAsJsonObject().entrySet();
                    if (entries.size() != 1) {
                        throw new JsonParseException("Expected definitions[" + i + "] to have one element, had " + entries.size());
                    }
                    Map.Entry<String, JsonElement> entry = Iterables.getOnlyElement(entries);
                    definitions.add(entry.getKey());
                    defaults.put(entry.getKey(), GsonHelper.convertToString(entry.getValue(), "definitions[" + i + "]"));
                } else {
                    throw new JsonParseException("Expected definitions[" + i + "] to be a JsonPrimitive or Object, was " + GsonHelper.getType(element));
                }
            }
        }

        Map<String, ShaderTextureSource> textures = new LinkedHashMap<>();
        if (json.has("textures")) {
            for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "textures").entrySet()) {
                textures.put(entry.getKey(), ShaderTextureSource.parse(entry.getValue()));
            }
        }

        return new ProgramDefinition(stages, definitions.toArray(String[]::new), Collections.unmodifiableMap(defaults), Collections.unmodifiableMap(textures));
    }
}
