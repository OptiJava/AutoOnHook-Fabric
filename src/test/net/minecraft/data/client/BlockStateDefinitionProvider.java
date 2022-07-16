/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.model.BlockStateModelGenerator;
import net.minecraft.data.client.model.BlockStateSupplier;
import net.minecraft.data.client.model.ModelIds;
import net.minecraft.data.client.model.SimpleModelSupplier;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockStateDefinitionProvider
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public BlockStateDefinitionProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(DataCache cache) {
        Path path = this.generator.getOutput();
        HashMap map = Maps.newHashMap();
        Consumer<BlockStateSupplier> consumer = blockStateSupplier -> {
            Block block = blockStateSupplier.getBlock();
            BlockStateSupplier blockStateSupplier2 = map.put(block, blockStateSupplier);
            if (blockStateSupplier2 != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        };
        HashMap map2 = Maps.newHashMap();
        HashSet set = Sets.newHashSet();
        BiConsumer<Identifier, Supplier<JsonElement>> biConsumer = (identifier, supplier) -> {
            Supplier supplier2 = map2.put(identifier, supplier);
            if (supplier2 != null) {
                throw new IllegalStateException("Duplicate model definition for " + identifier);
            }
        };
        Consumer<Item> consumer2 = set::add;
        new BlockStateModelGenerator(consumer, biConsumer, consumer2).register();
        new ItemModelGenerator(biConsumer).register();
        List list = Registry.BLOCK.stream().filter(block -> !map.containsKey(block)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            throw new IllegalStateException("Missing blockstate definitions for: " + list);
        }
        Registry.BLOCK.forEach(block -> {
            Item item = Item.BLOCK_ITEMS.get(block);
            if (item != null) {
                if (set.contains(item)) {
                    return;
                }
                Identifier identifier = ModelIds.getItemModelId(item);
                if (!map2.containsKey(identifier)) {
                    map2.put(identifier, new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
                }
            }
        });
        this.writeJsons(cache, path, map, BlockStateDefinitionProvider::getBlockStateJsonPath);
        this.writeJsons(cache, path, map2, BlockStateDefinitionProvider::getModelJsonPath);
    }

    private <T> void writeJsons(DataCache cache, Path root, Map<T, ? extends Supplier<JsonElement>> jsons, BiFunction<Path, T, Path> locator) {
        jsons.forEach((object, supplier) -> {
            Path path2 = (Path)locator.apply(root, object);
            try {
                DataProvider.writeToPath(GSON, cache, (JsonElement)supplier.get(), path2);
            }
            catch (Exception exception) {
                LOGGER.error("Couldn't save {}", (Object)path2, (Object)exception);
            }
        });
    }

    private static Path getBlockStateJsonPath(Path root, Block block) {
        Identifier identifier = Registry.BLOCK.getId(block);
        return root.resolve("assets/" + identifier.getNamespace() + "/blockstates/" + identifier.getPath() + ".json");
    }

    private static Path getModelJsonPath(Path root, Identifier id) {
        return root.resolve("assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Block State Definitions";
    }
}

