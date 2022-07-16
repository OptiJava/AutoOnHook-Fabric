/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.math.noise;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.util.math.noise.NoiseSampler;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.WorldGenRandom;

public class OctaveSimplexNoiseSampler
implements NoiseSampler {
    private final SimplexNoiseSampler[] octaveSamplers;
    private final double persistence;
    private final double lacunarity;

    public OctaveSimplexNoiseSampler(WorldGenRandom random, IntStream octaves) {
        this(random, octaves.boxed().collect(ImmutableList.toImmutableList()));
    }

    public OctaveSimplexNoiseSampler(WorldGenRandom random, List<Integer> octaves) {
        this(random, new IntRBTreeSet(octaves));
    }

    private OctaveSimplexNoiseSampler(WorldGenRandom random, IntSortedSet octaves) {
        int j;
        if (octaves.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -octaves.firstInt();
        int k = i + (j = octaves.lastInt()) + 1;
        if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        SimplexNoiseSampler simplexNoiseSampler = new SimplexNoiseSampler(random);
        int l = j;
        this.octaveSamplers = new SimplexNoiseSampler[k];
        if (l >= 0 && l < k && octaves.contains(0)) {
            this.octaveSamplers[l] = simplexNoiseSampler;
        }
        for (int m = l + 1; m < k; ++m) {
            if (m >= 0 && octaves.contains(l - m)) {
                this.octaveSamplers[m] = new SimplexNoiseSampler(random);
                continue;
            }
            random.skip(262);
        }
        if (j > 0) {
            long m = (long)(simplexNoiseSampler.sample(simplexNoiseSampler.originX, simplexNoiseSampler.originY, simplexNoiseSampler.originZ) * 9.223372036854776E18);
            ChunkRandom worldGenRandom = new ChunkRandom(m);
            for (int n = l - 1; n >= 0; --n) {
                if (n < k && octaves.contains(l - n)) {
                    this.octaveSamplers[n] = new SimplexNoiseSampler(worldGenRandom);
                    continue;
                }
                worldGenRandom.skip(262);
            }
        }
        this.lacunarity = Math.pow(2.0, j);
        this.persistence = 1.0 / (Math.pow(2.0, k) - 1.0);
    }

    public double sample(double x, double y, boolean useOrigin) {
        double d = 0.0;
        double e = this.lacunarity;
        double f = this.persistence;
        for (SimplexNoiseSampler simplexNoiseSampler : this.octaveSamplers) {
            if (simplexNoiseSampler != null) {
                d += simplexNoiseSampler.sample(x * e + (useOrigin ? simplexNoiseSampler.originX : 0.0), y * e + (useOrigin ? simplexNoiseSampler.originY : 0.0)) * f;
            }
            e /= 2.0;
            f *= 2.0;
        }
        return d;
    }

    @Override
    public double sample(double x, double y, double yScale, double yMax) {
        return this.sample(x, y, true) * 0.55;
    }
}

