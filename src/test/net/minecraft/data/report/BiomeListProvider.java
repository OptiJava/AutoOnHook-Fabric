/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeListProvider
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public BiomeListProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(DataCache cache) {
        Path path = this.generator.getOutput();
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : BuiltinRegistries.BIOME.getEntries()) {
            Path path2 = BiomeListProvider.getPath(path, entry.getKey().getValue());
            Biome biome = entry.getValue();
            Function<Supplier<Biome>, DataResult<Supplier<Biome>>> function = JsonOps.INSTANCE.withEncoder(Biome.REGISTRY_CODEC);
            try {
                Optional optional = function.apply(() -> biome).result();
                if (optional.isPresent()) {
                    DataProvider.writeToPath(GSON, cache, (JsonElement)optional.get(), path2);
                    continue;
                }
                LOGGER.error("Couldn't serialize biome {}", (Object)path2);
            }
            catch (IOException optional) {
                LOGGER.error("Couldn't save biome {}", (Object)path2, (Object)optional);
            }
        }
    }

    private static Path getPath(Path root, Identifier id) {
        return root.resolve("reports/biomes/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Biomes";
    }
}

