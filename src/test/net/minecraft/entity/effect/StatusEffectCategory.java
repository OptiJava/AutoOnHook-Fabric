/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.effect;

import net.minecraft.util.Formatting;

public enum StatusEffectCategory {
    BENEFICIAL(Formatting.BLUE),
    HARMFUL(Formatting.RED),
    NEUTRAL(Formatting.BLUE);

    private final Formatting formatting;

    private StatusEffectCategory(Formatting format) {
        this.formatting = format;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }
}

