/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;

public class EmptyEntry
extends LeafEntry {
    EmptyEntry(int i, int j, LootCondition[] lootConditions, LootFunction[] lootFunctions) {
        super(i, j, lootConditions, lootFunctions);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.EMPTY;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
    }

    public static LeafEntry.Builder<?> Serializer() {
        return EmptyEntry.builder(EmptyEntry::new);
    }

    public static class Serializer
    extends LeafEntry.Serializer<EmptyEntry> {
        @Override
        public EmptyEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] lootConditions, LootFunction[] lootFunctions) {
            return new EmptyEntry(i, j, lootConditions, lootFunctions);
        }

        @Override
        public /* synthetic */ LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            return this.fromJson(entryJson, context, weight, quality, conditions, functions);
        }
    }
}

