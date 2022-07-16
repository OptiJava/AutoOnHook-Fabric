/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public class RegistryDumpProvider
implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public RegistryDumpProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(DataCache cache) throws IOException {
        JsonObject jsonObject = new JsonObject();
        Registry.REGISTRIES.getIds().forEach(identifier -> jsonObject.add(identifier.toString(), RegistryDumpProvider.toJson(Registry.REGISTRIES.get((Identifier)identifier))));
        Path path = this.generator.getOutput().resolve("reports/registries.json");
        DataProvider.writeToPath(GSON, cache, jsonObject, path);
    }

    private static <T> JsonElement toJson(Registry<T> registry) {
        JsonObject jsonObject = new JsonObject();
        if (registry instanceof DefaultedRegistry) {
            Identifier identifier = ((DefaultedRegistry)registry).getDefaultId();
            jsonObject.addProperty("default", identifier.toString());
        }
        int identifier = Registry.REGISTRIES.getRawId(registry);
        jsonObject.addProperty("protocol_id", identifier);
        JsonObject jsonObject2 = new JsonObject();
        for (Identifier identifier2 : registry.getIds()) {
            T object = registry.get(identifier2);
            int i = registry.getRawId(object);
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("protocol_id", i);
            jsonObject2.add(identifier2.toString(), jsonObject3);
        }
        jsonObject.add("entries", jsonObject2);
        return jsonObject;
    }

    @Override
    public String getName() {
        return "Registry Dump";
    }
}

