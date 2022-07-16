/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum DoubleBlockHalf implements StringIdentifiable
{
    UPPER,
    LOWER;


    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this == UPPER ? "upper" : "lower";
    }
}

