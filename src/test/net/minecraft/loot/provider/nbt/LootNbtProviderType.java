/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.provider.nbt;

import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.JsonSerializer;

public class LootNbtProviderType
extends JsonSerializableType<LootNbtProvider> {
    public LootNbtProviderType(JsonSerializer<? extends LootNbtProvider> jsonSerializer) {
        super(jsonSerializer);
    }
}

