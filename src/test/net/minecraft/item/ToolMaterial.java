/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import net.minecraft.recipe.Ingredient;

public interface ToolMaterial {
    public int getDurability();

    public float getMiningSpeedMultiplier();

    public float getAttackDamage();

    public int getMiningLevel();

    public int getEnchantability();

    public Ingredient getRepairIngredient();
}

