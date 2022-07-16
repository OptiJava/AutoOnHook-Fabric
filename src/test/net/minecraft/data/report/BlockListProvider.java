/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public class BlockListProvider
implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public BlockListProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(DataCache cache) throws IOException {
        JsonObject jsonObject = new JsonObject();
        for (Block block : Registry.BLOCK) {
            JsonElement jsonArray;
            JsonElement jsonObject3;
            Identifier identifier = Registry.BLOCK.getId(block);
            JsonObject jsonObject2 = new JsonObject();
            StateManager<Block, BlockState> stateManager = block.getStateManager();
            if (!stateManager.getProperties().isEmpty()) {
                jsonObject3 = new JsonObject();
                for (Property property : stateManager.getProperties()) {
                    jsonArray = new JsonArray();
                    for (Comparable comparable : property.getValues()) {
                        ((JsonArray)jsonArray).add(Util.getValueAsString(property, comparable));
                    }
                    ((JsonObject)jsonObject3).add(property.getName(), jsonArray);
                }
                jsonObject2.add("properties", jsonObject3);
            }
            jsonObject3 = new JsonArray();
            for (BlockState blockState : stateManager.getStates()) {
                jsonArray = new JsonObject();
                JsonObject jsonObject4 = new JsonObject();
                for (Property<?> property2 : stateManager.getProperties()) {
                    jsonObject4.addProperty(property2.getName(), Util.getValueAsString(property2, blockState.get(property2)));
                }
                if (jsonObject4.size() > 0) {
                    ((JsonObject)jsonArray).add("properties", jsonObject4);
                }
                ((JsonObject)jsonArray).addProperty("id", Block.getRawIdFromState(blockState));
                if (blockState == block.getDefaultState()) {
                    ((JsonObject)jsonArray).addProperty("default", true);
                }
                ((JsonArray)jsonObject3).add(jsonArray);
            }
            jsonObject2.add("states", jsonObject3);
            jsonObject.add(identifier.toString(), jsonObject2);
        }
        Path path = this.generator.getOutput().resolve("reports/blocks.json");
        DataProvider.writeToPath(GSON, cache, jsonObject, path);
    }

    @Override
    public String getName() {
        return "Block List";
    }
}

