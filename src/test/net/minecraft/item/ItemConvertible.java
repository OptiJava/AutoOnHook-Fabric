/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import net.minecraft.item.Item;

/**
 * Represents an object that has an item form.
 */
public interface ItemConvertible {
    /**
     * Gets this object in its item form.
     */
    public Item asItem();
}

