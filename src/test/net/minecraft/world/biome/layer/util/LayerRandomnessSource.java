/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome.layer.util;

import net.minecraft.util.math.noise.PerlinNoiseSampler;

public interface LayerRandomnessSource {
    public int nextInt(int var1);

    public PerlinNoiseSampler getNoiseSampler();
}

