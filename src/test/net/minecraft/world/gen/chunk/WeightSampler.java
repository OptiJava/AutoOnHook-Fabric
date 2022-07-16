/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.chunk;

@FunctionalInterface
public interface WeightSampler {
    public static final WeightSampler DEFAULT = (d, i, j, k) -> d;

    public double sample(double var1, int var3, int var4, int var5);
}

