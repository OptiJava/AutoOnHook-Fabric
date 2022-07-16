/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.provider.nbt;

import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public interface LootNbtProvider {
    @Nullable
    public NbtElement getNbt(LootContext var1);

    public Set<LootContextParameter<?>> getRequiredParameters();

    public LootNbtProviderType getType();
}

