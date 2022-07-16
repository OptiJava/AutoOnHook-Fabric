/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum BedPart implements StringIdentifiable
{
    HEAD("head"),
    FOOT("foot");

    private final String name;

    private BedPart(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}

