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
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class DefaultSurfaceBuilder
extends SurfaceBuilder<TernarySurfaceConfig> {
    public DefaultSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
        super(codec);
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, TernarySurfaceConfig ternarySurfaceConfig) {
        this.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, ternarySurfaceConfig.getTopMaterial(), ternarySurfaceConfig.getUnderMaterial(), ternarySurfaceConfig.getUnderwaterMaterial(), l, m);
    }

    protected void generate(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState fluidBlock, BlockState topBlock, BlockState underBlock, BlockState underwaterBlock, int seaLevel, int i) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int j = (int)(noise / 3.0 + 3.0 + random.nextDouble() * 0.25);
        if (j == 0) {
            boolean bl = false;
            for (int k = height; k >= i; --k) {
                mutable.set(x, k, z);
                BlockState blockState = chunk.getBlockState(mutable);
                if (blockState.isAir()) {
                    bl = false;
                    continue;
                }
                if (!blockState.isOf(defaultBlock.getBlock())) continue;
                if (!bl) {
                    BlockState blockState2 = k >= seaLevel ? Blocks.AIR.getDefaultState() : (k == seaLevel - 1 ? (biome.getTemperature(mutable) < 0.15f ? Blocks.ICE.getDefaultState() : fluidBlock) : (k >= seaLevel - (7 + j) ? defaultBlock : underwaterBlock));
                    chunk.setBlockState(mutable, blockState2, false);
                }
                bl = true;
            }
        } else {
            BlockState bl = underBlock;
            int k = -1;
            for (int blockState = height; blockState >= i; --blockState) {
                mutable.set(x, blockState, z);
                BlockState blockState2 = chunk.getBlockState(mutable);
                if (blockState2.isAir()) {
                    k = -1;
                    continue;
                }
                if (!blockState2.isOf(defaultBlock.getBlock())) continue;
                if (k == -1) {
                    BlockState blockState3;
                    k = j;
                    if (blockState >= seaLevel + 2) {
                        blockState3 = topBlock;
                    } else if (blockState >= seaLevel - 1) {
                        bl = underBlock;
                        blockState3 = topBlock;
                    } else if (blockState >= seaLevel - 4) {
                        bl = underBlock;
                        blockState3 = underBlock;
                    } else if (blockState >= seaLevel - (7 + j)) {
                        blockState3 = bl;
                    } else {
                        bl = defaultBlock;
                        blockState3 = underwaterBlock;
                    }
                    chunk.setBlockState(mutable, blockState3, false);
                    continue;
                }
                if (k <= 0) continue;
                chunk.setBlockState(mutable, bl, false);
                if (--k != 0 || !bl.isOf(Blocks.SAND) || j <= 1) continue;
                k = random.nextInt(4) + Math.max(0, blockState - seaLevel);
                bl = bl.isOf(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.getDefaultState() : Blocks.SANDSTONE.getDefaultState();
            }
        }
    }
}

