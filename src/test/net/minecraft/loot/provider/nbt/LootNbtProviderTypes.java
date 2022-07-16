/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.provider.nbt;

import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.nbt.StorageLootNbtProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;
import net.minecraft.util.registry.Registry;

public class LootNbtProviderTypes {
    public static final LootNbtProviderType STORAGE = LootNbtProviderTypes.register("storage", new StorageLootNbtProvider.Serializer());
    public static final LootNbtProviderType CONTEXT = LootNbtProviderTypes.register("context", new ContextLootNbtProvider.Serializer());

    private static LootNbtProviderType register(String id, JsonSerializer<? extends LootNbtProvider> jsonSerializer) {
        return Registry.register(Registry.LOOT_NBT_PROVIDER_TYPE, new Identifier(id), new LootNbtProviderType(jsonSerializer));
    }

    public static Object createGsonSerializer() {
        return JsonSerializing.createSerializerBuilder(Registry.LOOT_NBT_PROVIDER_TYPE, "provider", "type", LootNbtProvider::getType).elementSerializer(CONTEXT, new ContextLootNbtProvider.CustomSerializer()).build();
    }
}

