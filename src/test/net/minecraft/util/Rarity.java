/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import net.minecraft.util.Formatting;

public enum Rarity {
    COMMON(Formatting.WHITE),
    UNCOMMON(Formatting.YELLOW),
    RARE(Formatting.AQUA),
    EPIC(Formatting.LIGHT_PURPLE);

    public final Formatting formatting;

    private Rarity(Formatting formatting) {
        this.formatting = formatting;
    }
}

