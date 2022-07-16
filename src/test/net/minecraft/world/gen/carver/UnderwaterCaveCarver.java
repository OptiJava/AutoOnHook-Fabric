/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CaveCarver;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class UnderwaterCaveCarver
extends CaveCarver {
    public UnderwaterCaveCarver(Codec<CaveCarverConfig> codec) {
        super(codec);
        this.alwaysCarvableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.SAND, Blocks.GRAVEL, Blocks.WATER, Blocks.LAVA, Blocks.OBSIDIAN, Blocks.PACKED_ICE});
    }

    @Override
    protected boolean isRegionUncarvable(Chunk chunk, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        return false;
    }

    @Override
    protected boolean carveAtPoint(CarverContext carverContext, CaveCarverConfig caveCarverConfig, Chunk chunk, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.Mutable mutable, BlockPos.Mutable mutable2, AquiferSampler aquiferSampler, MutableBoolean mutableBoolean) {
        return UnderwaterCaveCarver.carve(this, chunk, random, mutable, mutable2, aquiferSampler);
    }

    protected static boolean carve(Carver<?> carver, Chunk chunk, Random random, BlockPos.Mutable pos, BlockPos.Mutable downPos, AquiferSampler sampler) {
        if (sampler.apply(Carver.STONE_SOURCE, pos.getX(), pos.getY(), pos.getZ(), Double.NEGATIVE_INFINITY).isAir()) {
            return false;
        }
        BlockState blockState = chunk.getBlockState(pos);
        if (!carver.canAlwaysCarveBlock(blockState)) {
            return false;
        }
        if (pos.getY() == 10) {
            float f = random.nextFloat();
            if ((double)f < 0.25) {
                chunk.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), false);
                chunk.getBlockTickScheduler().schedule(pos, Blocks.MAGMA_BLOCK, 0);
            } else {
                chunk.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), false);
            }
            return true;
        }
        if (pos.getY() < 10) {
            chunk.setBlockState(pos, Blocks.LAVA.getDefaultState(), false);
            return false;
        }
        chunk.setBlockState(pos, WATER.getBlockState(), false);
        int f = chunk.getPos().x;
        int i = chunk.getPos().z;
        for (Direction direction : FluidBlock.field_34006) {
            downPos.set((Vec3i)pos, direction);
            if (ChunkSectionPos.getSectionCoord(downPos.getX()) == f && ChunkSectionPos.getSectionCoord(downPos.getZ()) == i && !chunk.getBlockState(downPos).isAir()) continue;
            chunk.getFluidTickScheduler().schedule(pos, WATER.getFluid(), 0);
            break;
        }
        return true;
    }
}

