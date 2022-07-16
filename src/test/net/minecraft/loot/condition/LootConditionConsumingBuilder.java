/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.condition;

import net.minecraft.loot.condition.LootCondition;

public interface LootConditionConsumingBuilder<T> {
    public T conditionally(LootCondition.Builder var1);

    public T getThis();
}

