/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface Nameable {
    public Text getName();

    default public boolean hasCustomName() {
        return this.getCustomName() != null;
    }

    default public Text getDisplayName() {
        return this.getName();
    }

    @Nullable
    default public Text getCustomName() {
        return null;
    }
}

