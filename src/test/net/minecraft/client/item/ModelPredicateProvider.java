/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Deprecated
@Environment(value=EnvType.CLIENT)
public interface ModelPredicateProvider {
    public float call(ItemStack var1, @Nullable ClientWorld var2, @Nullable LivingEntity var3, int var4);
}

