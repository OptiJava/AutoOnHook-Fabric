/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class DynamicEntry
extends LeafEntry {
    final Identifier name;

    DynamicEntry(Identifier identifier, int i, int j, LootCondition[] lootConditions, LootFunction[] lootFunctions) {
        super(i, j, lootConditions, lootFunctions);
        this.name = identifier;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.DYNAMIC;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        context.drop(this.name, lootConsumer);
    }

    public static LeafEntry.Builder<?> builder(Identifier name) {
        return DynamicEntry.builder((int weight, int quality, LootCondition[] conditions, LootFunction[] functions) -> new DynamicEntry(name, weight, quality, conditions, functions));
    }

    public static class Serializer
    extends LeafEntry.Serializer<DynamicEntry> {
        @Override
        public void addEntryFields(JsonObject jsonObject, DynamicEntry dynamicEntry, JsonSerializationContext jsonSerializationContext) {
            super.addEntryFields(jsonObject, dynamicEntry, jsonSerializationContext);
            jsonObject.addProperty("name", dynamicEntry.name.toString());
        }

        @Override
        protected DynamicEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] lootConditions, LootFunction[] lootFunctions) {
            Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "name"));
            return new DynamicEntry(identifier, i, j, lootConditions, lootFunctions);
        }

        @Override
        protected /* synthetic */ LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            return this.fromJson(entryJson, context, weight, quality, conditions, functions);
        }
    }
}

