/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class GiantTreeTaigaSurfaceBuilder
extends SurfaceBuilder<TernarySurfaceConfig> {
    public GiantTreeTaigaSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
        super(codec);
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, TernarySurfaceConfig ternarySurfaceConfig) {
        if (d > 1.75) {
            SurfaceBuilder.DEFAULT.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.COARSE_DIRT_CONFIG);
        } else if (d > -0.95) {
            SurfaceBuilder.DEFAULT.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.PODZOL_CONFIG);
        } else {
            SurfaceBuilder.DEFAULT.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.GRASS_CONFIG);
        }
    }
}

