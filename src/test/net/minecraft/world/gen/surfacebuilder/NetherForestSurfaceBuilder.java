/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.surfacebuilder;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.WorldGenRandom;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class NetherForestSurfaceBuilder
extends SurfaceBuilder<TernarySurfaceConfig> {
    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();
    protected long seed;
    private OctavePerlinNoiseSampler surfaceNoise;

    public NetherForestSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
        super(codec);
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, TernarySurfaceConfig ternarySurfaceConfig) {
        int o = l;
        int p = i & 0xF;
        int q = j & 0xF;
        double e = this.surfaceNoise.sample((double)i * 0.1, l, (double)j * 0.1);
        boolean bl = e > 0.15 + random.nextDouble() * 0.35;
        double f = this.surfaceNoise.sample((double)i * 0.1, 109.0, (double)j * 0.1);
        boolean bl2 = f > 0.25 + random.nextDouble() * 0.9;
        int r = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int s = -1;
        BlockState blockState3 = ternarySurfaceConfig.getUnderMaterial();
        for (int t = 127; t >= m; --t) {
            mutable.set(p, t, q);
            BlockState blockState4 = ternarySurfaceConfig.getTopMaterial();
            BlockState blockState5 = chunk.getBlockState(mutable);
            if (blockState5.isAir()) {
                s = -1;
                continue;
            }
            if (!blockState5.isOf(blockState.getBlock())) continue;
            if (s == -1) {
                boolean bl3 = false;
                if (r <= 0) {
                    bl3 = true;
                    blockState3 = ternarySurfaceConfig.getUnderMaterial();
                }
                if (bl) {
                    blockState4 = ternarySurfaceConfig.getUnderMaterial();
                } else if (bl2) {
                    blockState4 = ternarySurfaceConfig.getUnderwaterMaterial();
                }
                if (t < o && bl3) {
                    blockState4 = blockState2;
                }
                s = r;
                if (t >= o - 1) {
                    chunk.setBlockState(mutable, blockState4, false);
                    continue;
                }
                chunk.setBlockState(mutable, blockState3, false);
                continue;
            }
            if (s <= 0) continue;
            --s;
            chunk.setBlockState(mutable, blockState3, false);
        }
    }

    @Override
    public void initSeed(long seed) {
        if (this.seed != seed || this.surfaceNoise == null) {
            this.surfaceNoise = new OctavePerlinNoiseSampler((WorldGenRandom)new ChunkRandom(seed), ImmutableList.of(Integer.valueOf(0)));
        }
        this.seed = seed;
    }
}
