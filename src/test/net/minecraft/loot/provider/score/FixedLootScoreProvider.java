/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.provider.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.jetbrains.annotations.Nullable;

public class FixedLootScoreProvider
implements LootScoreProvider {
    final String name;

    FixedLootScoreProvider(String string) {
        this.name = string;
    }

    public static LootScoreProvider create(String name) {
        return new FixedLootScoreProvider(name);
    }

    @Override
    public LootScoreProviderType getType() {
        return LootScoreProviderTypes.FIXED;
    }

    public String getName() {
        return this.name;
    }

    @Override
    @Nullable
    public String getName(LootContext context) {
        return this.name;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of();
    }

    public static class Serializer
    implements JsonSerializer<FixedLootScoreProvider> {
        @Override
        public void toJson(JsonObject jsonObject, FixedLootScoreProvider fixedLootScoreProvider, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("name", fixedLootScoreProvider.name);
        }

        @Override
        public FixedLootScoreProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            String string = JsonHelper.getString(jsonObject, "name");
            return new FixedLootScoreProvider(string);
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

