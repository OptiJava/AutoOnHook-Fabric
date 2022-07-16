/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an object which can be cleared.
 */
public interface Clearable {
    public void clear();

    public static void clear(@Nullable Object o) {
        if (o instanceof Clearable) {
            ((Clearable)o).clear();
        }
    }
}

