/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetLootTableLootFunction
extends ConditionalLootFunction {
    final Identifier id;
    final long seed;

    SetLootTableLootFunction(LootCondition[] lootConditions, Identifier identifier, long l) {
        super(lootConditions);
        this.id = identifier;
        this.seed = l;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (stack.isEmpty()) {
            return stack;
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("LootTable", this.id.toString());
        if (this.seed != 0L) {
            nbtCompound.putLong("LootTableSeed", this.seed);
        }
        stack.getOrCreateNbt().put("BlockEntityTag", nbtCompound);
        return stack;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        if (reporter.hasTable(this.id)) {
            reporter.report("Table " + this.id + " is recursively called");
            return;
        }
        super.validate(reporter);
        LootTable lootTable = reporter.getTable(this.id);
        if (lootTable == null) {
            reporter.report("Unknown loot table called " + this.id);
        } else {
            lootTable.validate(reporter.withTable("->{" + this.id + "}", this.id));
        }
    }

    public static ConditionalLootFunction.Builder<?> builder(Identifier id) {
        return SetLootTableLootFunction.builder((LootCondition[] conditions) -> new SetLootTableLootFunction((LootCondition[])conditions, id, 0L));
    }

    public static ConditionalLootFunction.Builder<?> builder(Identifier id, long seed) {
        return SetLootTableLootFunction.builder((LootCondition[] conditions) -> new SetLootTableLootFunction((LootCondition[])conditions, id, seed));
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetLootTableLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetLootTableLootFunction setLootTableLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, setLootTableLootFunction, jsonSerializationContext);
            jsonObject.addProperty("name", setLootTableLootFunction.id.toString());
            if (setLootTableLootFunction.seed != 0L) {
                jsonObject.addProperty("seed", setLootTableLootFunction.seed);
            }
        }

        @Override
        public SetLootTableLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "name"));
            long l = JsonHelper.getLong(jsonObject, "seed", 0L);
            return new SetLootTableLootFunction(lootConditions, identifier, l);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

