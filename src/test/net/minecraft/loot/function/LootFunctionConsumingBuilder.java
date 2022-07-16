/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.function;

import net.minecraft.loot.function.LootFunction;

public interface LootFunctionConsumingBuilder<T> {
    public T apply(LootFunction.Builder var1);

    public T getThis();
}

