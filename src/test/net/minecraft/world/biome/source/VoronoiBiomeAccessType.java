/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome.source;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeAccessType;
import net.minecraft.world.biome.source.SeedMixer;

public enum VoronoiBiomeAccessType implements BiomeAccessType
{
    INSTANCE;

    private static final int field_30979 = 2;
    private static final int field_30980 = 4;
    private static final int field_30981 = 3;

    @Override
    public Biome getBiome(long seed, int x, int y, int z, BiomeAccess.Storage storage) {
        int bl2;
        int bl;
        int p;
        int i = x - 2;
        int j = y - 2;
        int k = z - 2;
        int l = i >> 2;
        int m = j >> 2;
        int n = k >> 2;
        double d = (double)(i & 3) / 4.0;
        double e = (double)(j & 3) / 4.0;
        double f = (double)(k & 3) / 4.0;
        int o = 0;
        double g = Double.POSITIVE_INFINITY;
        for (p = 0; p < 8; ++p) {
            double u;
            double t;
            double h;
            boolean bl3;
            int s;
            int r;
            bl = (p & 4) == 0 ? 1 : 0;
            int q = bl != 0 ? l : l + 1;
            double v = VoronoiBiomeAccessType.calcSquaredDistance(seed, q, r = (bl2 = (p & 2) == 0 ? 1 : 0) != 0 ? m : m + 1, s = (bl3 = (p & 1) == 0) ? n : n + 1, h = bl != 0 ? d : d - 1.0, t = bl2 != 0 ? e : e - 1.0, u = bl3 ? f : f - 1.0);
            if (!(g > v)) continue;
            o = p;
            g = v;
        }
        p = (o & 4) == 0 ? l : l + 1;
        bl = (o & 2) == 0 ? m : m + 1;
        bl2 = (o & 1) == 0 ? n : n + 1;
        return storage.getBiomeForNoiseGen(p, bl, bl2);
    }

    private static double calcSquaredDistance(long seed, int x, int y, int z, double xFraction, double yFraction, double zFraction) {
        long l = seed;
        l = SeedMixer.mixSeed(l, x);
        l = SeedMixer.mixSeed(l, y);
        l = SeedMixer.mixSeed(l, z);
        l = SeedMixer.mixSeed(l, x);
        l = SeedMixer.mixSeed(l, y);
        l = SeedMixer.mixSeed(l, z);
        double d = VoronoiBiomeAccessType.distribute(l);
        l = SeedMixer.mixSeed(l, seed);
        double e = VoronoiBiomeAccessType.distribute(l);
        l = SeedMixer.mixSeed(l, seed);
        double f = VoronoiBiomeAccessType.distribute(l);
        return VoronoiBiomeAccessType.square(zFraction + f) + VoronoiBiomeAccessType.square(yFraction + e) + VoronoiBiomeAccessType.square(xFraction + d);
    }

    private static double distribute(long seed) {
        double d = (double)Math.floorMod(seed >> 24, 1024) / 1024.0;
        return (d - 0.5) * 0.9;
    }

    private static double square(double d) {
        return d * d;
    }
}

