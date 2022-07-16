/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.function;

import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.JsonSerializer;

public class LootFunctionType
extends JsonSerializableType<LootFunction> {
    public LootFunctionType(JsonSerializer<? extends LootFunction> jsonSerializer) {
        super(jsonSerializer);
    }
}

