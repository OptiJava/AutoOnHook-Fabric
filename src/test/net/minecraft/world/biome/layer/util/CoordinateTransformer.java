/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome.layer.util;

import net.minecraft.world.biome.BiomeIds;

public interface CoordinateTransformer
extends BiomeIds {
    public int transformX(int var1);

    public int transformZ(int var1);
}

