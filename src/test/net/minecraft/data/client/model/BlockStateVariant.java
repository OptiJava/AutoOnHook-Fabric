/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.client.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.data.client.model.VariantSetting;

/**
 * The supplier for a {@code ModelVariant}'s JSON representation.
 */
public class BlockStateVariant
implements Supplier<JsonElement> {
    private final Map<VariantSetting<?>, VariantSetting.Value> properties = Maps.newLinkedHashMap();

    public <T> BlockStateVariant put(VariantSetting<T> key, T value) {
        VariantSetting.Value value2 = this.properties.put(key, key.evaluate(value));
        if (value2 != null) {
            throw new IllegalStateException("Replacing value of " + value2 + " with " + value);
        }
        return this;
    }

    public static BlockStateVariant create() {
        return new BlockStateVariant();
    }

    public static BlockStateVariant union(BlockStateVariant first, BlockStateVariant second) {
        BlockStateVariant blockStateVariant = new BlockStateVariant();
        blockStateVariant.properties.putAll(first.properties);
        blockStateVariant.properties.putAll(second.properties);
        return blockStateVariant;
    }

    @Override
    public JsonElement get() {
        JsonObject jsonObject = new JsonObject();
        this.properties.values().forEach(value -> value.writeTo(jsonObject));
        return jsonObject;
    }

    public static JsonElement toJson(List<BlockStateVariant> variants) {
        if (variants.size() == 1) {
            return variants.get(0).get();
        }
        JsonArray jsonArray = new JsonArray();
        variants.forEach(blockStateVariant -> jsonArray.add(blockStateVariant.get()));
        return jsonArray;
    }

    @Override
    public /* synthetic */ Object get() {
        return this.get();
    }
}

