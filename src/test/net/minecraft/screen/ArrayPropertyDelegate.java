/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen;

import net.minecraft.screen.PropertyDelegate;

/**
 * A {@link PropertyDelegate} that is implemented using an int array.
 */
public class ArrayPropertyDelegate
implements PropertyDelegate {
    private final int[] data;

    public ArrayPropertyDelegate(int size) {
        this.data = new int[size];
    }

    @Override
    public int get(int index) {
        return this.data[index];
    }

    @Override
    public void set(int index, int value) {
        this.data[index] = value;
    }

    @Override
    public int size() {
        return this.data.length;
    }
}

