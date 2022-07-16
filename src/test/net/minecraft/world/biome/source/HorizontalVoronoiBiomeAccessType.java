/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome.source;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeAccessType;
import net.minecraft.world.biome.source.VoronoiBiomeAccessType;

public enum HorizontalVoronoiBiomeAccessType implements BiomeAccessType
{
    INSTANCE;


    @Override
    public Biome getBiome(long seed, int x, int y, int z, BiomeAccess.Storage storage) {
        return VoronoiBiomeAccessType.INSTANCE.getBiome(seed, x, 0, z, storage);
    }
}

