/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.provider.number;

import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextAware;
import net.minecraft.loot.provider.number.LootNumberProviderType;

public interface LootNumberProvider
extends LootContextAware {
    public float nextFloat(LootContext var1);

    default public int nextInt(LootContext context) {
        return Math.round(this.nextFloat(context));
    }

    public LootNumberProviderType getType();
}

