/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity;

import net.minecraft.inventory.Inventory;
import net.minecraft.util.annotation.Debug;

public interface InventoryOwner {
    @Debug
    public Inventory getInventory();
}

