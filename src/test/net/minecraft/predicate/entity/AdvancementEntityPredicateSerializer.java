/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.predicate.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.condition.LootCondition;

public class AdvancementEntityPredicateSerializer {
    public static final AdvancementEntityPredicateSerializer INSTANCE = new AdvancementEntityPredicateSerializer();
    private final Gson gson = LootGsons.getConditionGsonBuilder().create();

    public final JsonElement conditionsToJson(LootCondition[] conditions) {
        return this.gson.toJsonTree(conditions);
    }
}

