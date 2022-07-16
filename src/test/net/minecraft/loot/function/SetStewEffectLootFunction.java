/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
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

public class SetStewEffectLootFunction
extends ConditionalLootFunction {
    final Map<StatusEffect, LootNumberProvider> effects;

    SetStewEffectLootFunction(LootCondition[] lootConditions, Map<StatusEffect, LootNumberProvider> map) {
        super(lootConditions);
        this.effects = ImmutableMap.copyOf(map);
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_STEW_EFFECT;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.effects.values().stream().flatMap(numberProvider -> numberProvider.getRequiredParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (!stack.isOf(Items.SUSPICIOUS_STEW) || this.effects.isEmpty()) {
            return stack;
        }
        Random random = context.getRandom();
        int i = random.nextInt(this.effects.size());
        Map.Entry<StatusEffect, LootNumberProvider> entry = Iterables.get(this.effects.entrySet(), i);
        StatusEffect statusEffect = entry.getKey();
        int j = entry.getValue().nextInt(context);
        if (!statusEffect.isInstant()) {
            j *= 20;
        }
        SuspiciousStewItem.addEffectToStew(stack, statusEffect, j);
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final Map<StatusEffect, LootNumberProvider> map = Maps.newHashMap();

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder withEffect(StatusEffect effect, LootNumberProvider durationRange) {
            this.map.put(effect, durationRange);
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetStewEffectLootFunction(this.getConditions(), this.map);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetStewEffectLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetStewEffectLootFunction setStewEffectLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, setStewEffectLootFunction, jsonSerializationContext);
            if (!setStewEffectLootFunction.effects.isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (StatusEffect statusEffect : setStewEffectLootFunction.effects.keySet()) {
                    JsonObject jsonObject2 = new JsonObject();
                    Identifier identifier = Registry.STATUS_EFFECT.getId(statusEffect);
                    if (identifier == null) {
                        throw new IllegalArgumentException("Don't know how to serialize mob effect " + statusEffect);
                    }
                    jsonObject2.add("type", new JsonPrimitive(identifier.toString()));
                    jsonObject2.add("duration", jsonSerializationContext.serialize(setStewEffectLootFunction.effects.get(statusEffect)));
                    jsonArray.add(jsonObject2);
                }
                jsonObject.add("effects", jsonArray);
            }
        }

        @Override
        public SetStewEffectLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            HashMap<StatusEffect, LootNumberProvider> map = Maps.newHashMap();
            if (jsonObject.has("effects")) {
                JsonArray jsonArray = JsonHelper.getArray(jsonObject, "effects");
                for (JsonElement jsonElement : jsonArray) {
                    String string = JsonHelper.getString(jsonElement.getAsJsonObject(), "type");
                    StatusEffect statusEffect = Registry.STATUS_EFFECT.getOrEmpty(new Identifier(string)).orElseThrow(() -> new JsonSyntaxException("Unknown mob effect '" + string + "'"));
                    LootNumberProvider lootNumberProvider = JsonHelper.deserialize(jsonElement.getAsJsonObject(), "duration", jsonDeserializationContext, LootNumberProvider.class);
                    map.put(statusEffect, lootNumberProvider);
                }
            }
            return new SetStewEffectLootFunction(lootConditions, map);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}
