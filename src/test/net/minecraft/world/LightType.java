/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

public enum LightType {
    SKY(15),
    BLOCK(0);

    public final int value;

    private LightType(int value) {
        this.value = value;
    }
}

