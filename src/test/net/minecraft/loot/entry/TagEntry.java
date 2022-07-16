/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class TagEntry
extends LeafEntry {
    final Tag<Item> name;
    final boolean expand;

    TagEntry(Tag<Item> tag, boolean bl, int i, int j, LootCondition[] lootConditions, LootFunction[] lootFunctions) {
        super(i, j, lootConditions, lootFunctions);
        this.name = tag;
        this.expand = bl;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.TAG;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        this.name.values().forEach(item -> lootConsumer.accept(new ItemStack((ItemConvertible)item)));
    }

    private boolean grow(LootContext context, Consumer<LootChoice> lootChoiceExpander) {
        if (this.test(context)) {
            for (final Item item : this.name.values()) {
                lootChoiceExpander.accept(new LeafEntry.Choice(){

                    @Override
                    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
                        lootConsumer.accept(new ItemStack(item));
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootChoice> consumer) {
        if (this.expand) {
            return this.grow(lootContext, consumer);
        }
        return super.expand(lootContext, consumer);
    }

    public static LeafEntry.Builder<?> builder(Tag<Item> name) {
        return TagEntry.builder((int weight, int quality, LootCondition[] conditions, LootFunction[] functions) -> new TagEntry(name, false, weight, quality, conditions, functions));
    }

    public static LeafEntry.Builder<?> expandBuilder(Tag<Item> name) {
        return TagEntry.builder((int weight, int quality, LootCondition[] conditions, LootFunction[] functions) -> new TagEntry(name, true, weight, quality, conditions, functions));
    }

    public static class Serializer
    extends LeafEntry.Serializer<TagEntry> {
        @Override
        public void addEntryFields(JsonObject jsonObject, TagEntry tagEntry, JsonSerializationContext jsonSerializationContext) {
            super.addEntryFields(jsonObject, tagEntry, jsonSerializationContext);
            jsonObject.addProperty("name", ServerTagManagerHolder.getTagManager().getTagId(Registry.ITEM_KEY, tagEntry.name, () -> new IllegalStateException("Unknown item tag")).toString());
            jsonObject.addProperty("expand", tagEntry.expand);
        }

        @Override
        protected TagEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] lootConditions, LootFunction[] lootFunctions) {
            Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "name"));
            Tag<Item> tag = ServerTagManagerHolder.getTagManager().getTag(Registry.ITEM_KEY, identifier, id -> new JsonParseException("Can't find tag: " + id));
            boolean bl = JsonHelper.getBoolean(jsonObject, "expand");
            return new TagEntry(tag, bl, i, j, lootConditions, lootFunctions);
        }

        @Override
        protected /* synthetic */ LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            return this.fromJson(entryJson, context, weight, quality, conditions, functions);
        }
    }
}

