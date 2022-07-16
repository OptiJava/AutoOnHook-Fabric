/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.provider.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public final class BinomialLootNumberProvider
implements LootNumberProvider {
    final LootNumberProvider n;
    final LootNumberProvider p;

    BinomialLootNumberProvider(LootNumberProvider lootNumberProvider, LootNumberProvider lootNumberProvider2) {
        this.n = lootNumberProvider;
        this.p = lootNumberProvider2;
    }

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.BINOMIAL;
    }

    @Override
    public int nextInt(LootContext context) {
        int i = this.n.nextInt(context);
        float f = this.p.nextFloat(context);
        Random random = context.getRandom();
        int j = 0;
        for (int k = 0; k < i; ++k) {
            if (!(random.nextFloat() < f)) continue;
            ++j;
        }
        return j;
    }

    @Override
    public float nextFloat(LootContext context) {
        return this.nextInt(context);
    }

    public static BinomialLootNumberProvider create(int n, float p) {
        return new BinomialLootNumberProvider(ConstantLootNumberProvider.create(n), ConstantLootNumberProvider.create(p));
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Sets.union(this.n.getRequiredParameters(), this.p.getRequiredParameters());
    }

    public static class Serializer
    implements JsonSerializer<BinomialLootNumberProvider> {
        @Override
        public BinomialLootNumberProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootNumberProvider lootNumberProvider = JsonHelper.deserialize(jsonObject, "n", jsonDeserializationContext, LootNumberProvider.class);
            LootNumberProvider lootNumberProvider2 = JsonHelper.deserialize(jsonObject, "p", jsonDeserializationContext, LootNumberProvider.class);
            return new BinomialLootNumberProvider(lootNumberProvider, lootNumberProvider2);
        }

        @Override
        public void toJson(JsonObject jsonObject, BinomialLootNumberProvider binomialLootNumberProvider, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("n", jsonSerializationContext.serialize(binomialLootNumberProvider.n));
            jsonObject.add("p", jsonSerializationContext.serialize(binomialLootNumberProvider.p));
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

