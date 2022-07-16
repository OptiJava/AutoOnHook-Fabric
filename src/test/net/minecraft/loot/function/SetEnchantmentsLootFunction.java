/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class SetEnchantmentsLootFunction
extends ConditionalLootFunction {
    final Map<Enchantment, LootNumberProvider> enchantments;
    final boolean add;

    SetEnchantmentsLootFunction(LootCondition[] lootConditions, Map<Enchantment, LootNumberProvider> map, boolean bl) {
        super(lootConditions);
        this.enchantments = ImmutableMap.copyOf(map);
        this.add = bl;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_ENCHANTMENTS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.enchantments.values().stream().flatMap(numberProvider -> numberProvider.getRequiredParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        Object2IntOpenHashMap<Enchantment> object2IntMap = new Object2IntOpenHashMap<Enchantment>();
        this.enchantments.forEach((enchantment, numberProvider) -> object2IntMap.put((Enchantment)enchantment, numberProvider.nextInt(context)));
        if (stack.getItem() == Items.BOOK) {
            ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            object2IntMap.forEach((enchantment, level) -> EnchantedBookItem.addEnchantment(itemStack, new EnchantmentLevelEntry((Enchantment)enchantment, (int)level)));
            return itemStack;
        }
        Map<Enchantment, Integer> itemStack = EnchantmentHelper.get(stack);
        if (this.add) {
            object2IntMap.forEach((enchantment, level) -> SetEnchantmentsLootFunction.addEnchantmentToMap(itemStack, enchantment, Math.max(itemStack.getOrDefault(enchantment, 0) + level, 0)));
        } else {
            object2IntMap.forEach((enchantment, level) -> SetEnchantmentsLootFunction.addEnchantmentToMap(itemStack, enchantment, Math.max(level, 0)));
        }
        EnchantmentHelper.set(itemStack, stack);
        return stack;
    }

    private static void addEnchantmentToMap(Map<Enchantment, Integer> map, Enchantment enchantment, int level) {
        if (level == 0) {
            map.remove(enchantment);
        } else {
            map.put(enchantment, level);
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetEnchantmentsLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetEnchantmentsLootFunction setEnchantmentsLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, setEnchantmentsLootFunction, jsonSerializationContext);
            JsonObject jsonObject2 = new JsonObject();
            setEnchantmentsLootFunction.enchantments.forEach((enchantment, lootNumberProvider) -> {
                Identifier identifier = Registry.ENCHANTMENT.getId((Enchantment)enchantment);
                if (identifier == null) {
                    throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
                }
                jsonObject2.add(identifier.toString(), jsonSerializationContext.serialize(lootNumberProvider));
            });
            jsonObject.add("enchantments", jsonObject2);
            jsonObject.addProperty("add", setEnchantmentsLootFunction.add);
        }

        @Override
        public SetEnchantmentsLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            HashMap<Enchantment, LootNumberProvider> map = Maps.newHashMap();
            if (jsonObject.has("enchantments")) {
                JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "enchantments");
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    String string = entry.getKey();
                    JsonElement jsonElement = entry.getValue();
                    Enchantment enchantment = Registry.ENCHANTMENT.getOrEmpty(new Identifier(string)).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + string + "'"));
                    LootNumberProvider lootNumberProvider = (LootNumberProvider)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)LootNumberProvider.class));
                    map.put(enchantment, lootNumberProvider);
                }
            }
            boolean jsonObject2 = JsonHelper.getBoolean(jsonObject, "add", false);
            return new SetEnchantmentsLootFunction(lootConditions, map, jsonObject2);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final Map<Enchantment, LootNumberProvider> enchantments = Maps.newHashMap();
        private final boolean add;

        public Builder() {
            this(false);
        }

        public Builder(boolean add) {
            this.add = add;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder enchantment(Enchantment enchantment, LootNumberProvider level) {
            this.enchantments.put(enchantment, level);
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetEnchantmentsLootFunction(this.getConditions(), this.enchantments, this.add);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

