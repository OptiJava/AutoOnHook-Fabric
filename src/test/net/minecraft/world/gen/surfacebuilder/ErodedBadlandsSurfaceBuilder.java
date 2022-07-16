/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.BadlandsSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class ErodedBadlandsSurfaceBuilder
extends BadlandsSurfaceBuilder {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.getDefaultState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.getDefaultState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.getDefaultState();

    public ErodedBadlandsSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
        super(codec);
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, TernarySurfaceConfig ternarySurfaceConfig) {
        double e = 0.0;
        double f = Math.min(Math.abs(d), this.heightCutoffNoise.sample((double)i * 0.25, (double)j * 0.25, false) * 15.0);
        if (f > 0.0) {
            double g = 0.001953125;
            e = f * f * 2.5;
            double h = Math.abs(this.heightNoise.sample((double)i * 0.001953125, (double)j * 0.001953125, false));
            double o = Math.ceil(h * 50.0) + 14.0;
            if (e > o) {
                e = o;
            }
            e += 64.0;
        }
        int g = i & 0xF;
        int p = j & 0xF;
        BlockState h = WHITE_TERRACOTTA;
        SurfaceConfig surfaceConfig = biome.getGenerationSettings().getSurfaceConfig();
        BlockState o = surfaceConfig.getUnderMaterial();
        BlockState blockState3 = surfaceConfig.getTopMaterial();
        BlockState blockState4 = o;
        int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        boolean bl = Math.cos(d / 3.0 * Math.PI) > 0.0;
        int r = -1;
        boolean bl2 = false;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int s = Math.max(k, (int)e + 1); s >= m; --s) {
            BlockState blockState5;
            mutable.set(g, s, p);
            if (chunk.getBlockState(mutable).isAir() && s < (int)e) {
                chunk.setBlockState(mutable, blockState, false);
            }
            if ((blockState5 = chunk.getBlockState(mutable)).isAir()) {
                r = -1;
                continue;
            }
            if (!blockState5.isOf(blockState.getBlock())) continue;
            if (r == -1) {
                bl2 = false;
                if (q <= 0) {
                    h = Blocks.AIR.getDefaultState();
                    blockState4 = blockState;
                } else if (s >= l - 4 && s <= l + 1) {
                    h = WHITE_TERRACOTTA;
                    blockState4 = o;
                }
                if (s < l && (h == null || h.isAir())) {
                    h = blockState2;
                }
                r = q + Math.max(0, s - l);
                if (s >= l - 1) {
                    if (s > l + 3 + q) {
                        BlockState blockState6 = s < 64 || s > 127 ? ORANGE_TERRACOTTA : (bl ? TERRACOTTA : this.calculateLayerBlockState(i, s, j));
                        chunk.setBlockState(mutable, blockState6, false);
                        continue;
                    }
                    chunk.setBlockState(mutable, blockState3, false);
                    bl2 = true;
                    continue;
                }
                chunk.setBlockState(mutable, blockState4, false);
                if (!blockState4.isOf(Blocks.WHITE_TERRACOTTA) && !blockState4.isOf(Blocks.ORANGE_TERRACOTTA) && !blockState4.isOf(Blocks.MAGENTA_TERRACOTTA) && !blockState4.isOf(Blocks.LIGHT_BLUE_TERRACOTTA) && !blockState4.isOf(Blocks.YELLOW_TERRACOTTA) && !blockState4.isOf(Blocks.LIME_TERRACOTTA) && !blockState4.isOf(Blocks.PINK_TERRACOTTA) && !blockState4.isOf(Blocks.GRAY_TERRACOTTA) && !blockState4.isOf(Blocks.LIGHT_GRAY_TERRACOTTA) && !blockState4.isOf(Blocks.CYAN_TERRACOTTA) && !blockState4.isOf(Blocks.PURPLE_TERRACOTTA) && !blockState4.isOf(Blocks.BLUE_TERRACOTTA) && !blockState4.isOf(Blocks.BROWN_TERRACOTTA) && !blockState4.isOf(Blocks.GREEN_TERRACOTTA) && !blockState4.isOf(Blocks.RED_TERRACOTTA) && !blockState4.isOf(Blocks.BLACK_TERRACOTTA)) continue;
                chunk.setBlockState(mutable, ORANGE_TERRACOTTA, false);
                continue;
            }
            if (r <= 0) continue;
            --r;
            if (bl2) {
                chunk.setBlockState(mutable, ORANGE_TERRACOTTA, false);
                continue;
            }
            chunk.setBlockState(mutable, this.calculateLayerBlockState(i, s, j), false);
        }
    }
}

