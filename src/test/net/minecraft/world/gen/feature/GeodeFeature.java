/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.WorldGenRandom;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.GeodeCrackConfig;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;
import net.minecraft.world.gen.feature.GeodeLayerConfig;
import net.minecraft.world.gen.feature.GeodeLayerThicknessConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class GeodeFeature
extends Feature<GeodeFeatureConfig> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public GeodeFeature(Codec<GeodeFeatureConfig> codec) {
        super(codec);
    }

    /*
     * Could not resolve type clashes
     */
    @Override
    public boolean generate(FeatureContext<GeodeFeatureConfig> context) {
        BlockState blockState;
        int o;
        int n;
        GeodeFeatureConfig geodeFeatureConfig = context.getConfig();
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        int i = geodeFeatureConfig.minGenOffset;
        int j = geodeFeatureConfig.maxGenOffset;
        LinkedList<Pair<BlockPos, Integer>> list = Lists.newLinkedList();
        int k = geodeFeatureConfig.distributionPoints.get(random);
        ChunkRandom chunkRandom = new ChunkRandom(structureWorldAccess.getSeed());
        DoublePerlinNoiseSampler doublePerlinNoiseSampler = DoublePerlinNoiseSampler.create((WorldGenRandom)chunkRandom, -4, 1.0);
        LinkedList<BlockPos> list2 = Lists.newLinkedList();
        double d = (double)k / (double)geodeFeatureConfig.outerWallDistance.getMax();
        GeodeLayerThicknessConfig geodeLayerThicknessConfig = geodeFeatureConfig.layerThicknessConfig;
        GeodeLayerConfig geodeLayerConfig = geodeFeatureConfig.layerConfig;
        GeodeCrackConfig geodeCrackConfig = geodeFeatureConfig.crackConfig;
        double e = 1.0 / Math.sqrt(geodeLayerThicknessConfig.filling);
        double f = 1.0 / Math.sqrt(geodeLayerThicknessConfig.innerLayer + d);
        double g = 1.0 / Math.sqrt(geodeLayerThicknessConfig.middleLayer + d);
        double h = 1.0 / Math.sqrt(geodeLayerThicknessConfig.outerLayer + d);
        double l = 1.0 / Math.sqrt(geodeCrackConfig.baseCrackSize + random.nextDouble() / 2.0 + (k > 3 ? d : 0.0));
        boolean bl = (double)random.nextFloat() < geodeCrackConfig.generateCrackChance;
        int m = 0;
        for (n = 0; n < k; ++n) {
            int q;
            int p;
            o = geodeFeatureConfig.outerWallDistance.get(random);
            BlockPos blockPos2 = blockPos.add(o, p = geodeFeatureConfig.outerWallDistance.get(random), q = geodeFeatureConfig.outerWallDistance.get(random));
            blockState = structureWorldAccess.getBlockState(blockPos2);
            if ((blockState.isAir() || blockState.isIn(BlockTags.GEODE_INVALID_BLOCKS)) && ++m > geodeFeatureConfig.invalidBlocksThreshold) {
                return false;
            }
            list.add(Pair.of(blockPos2, geodeFeatureConfig.pointOffset.get(random)));
        }
        if (bl) {
            n = random.nextInt(4);
            o = k * 2 + 1;
            if (n == 0) {
                list2.add(blockPos.add(o, 7, 0));
                list2.add(blockPos.add(o, 5, 0));
                list2.add(blockPos.add(o, 1, 0));
            } else if (n == 1) {
                list2.add(blockPos.add(0, 7, o));
                list2.add(blockPos.add(0, 5, o));
                list2.add(blockPos.add(0, 1, o));
            } else if (n == 2) {
                list2.add(blockPos.add(o, 7, o));
                list2.add(blockPos.add(o, 5, o));
                list2.add(blockPos.add(o, 1, o));
            } else {
                list2.add(blockPos.add(0, 7, 0));
                list2.add(blockPos.add(0, 5, 0));
                list2.add(blockPos.add(0, 1, 0));
            }
        }
        ArrayList<BlockPos> n2 = Lists.newArrayList();
        Predicate<BlockState> o2 = GeodeFeature.notInBlockTagPredicate(geodeFeatureConfig.layerConfig.cannotReplace);
        for (BlockPos q : BlockPos.iterate(blockPos.add(i, i, i), blockPos.add(j, j, j))) {
            double blockPos2 = doublePerlinNoiseSampler.sample(q.getX(), q.getY(), q.getZ()) * geodeFeatureConfig.noiseMultiplier;
            double r = 0.0;
            double s = 0.0;
            for (Object pair : list) {
                r += MathHelper.fastInverseSqrt(q.getSquaredDistance((Vec3i)((Pair)pair).getFirst()) + (double)((Integer)((Pair)pair).getSecond()).intValue()) + blockPos2;
            }
            for (Object pair : list2) {
                s += MathHelper.fastInverseSqrt(q.getSquaredDistance((Vec3i)pair) + (double)geodeCrackConfig.crackPointOffset) + blockPos2;
            }
            if (r < h) continue;
            if (bl && s >= l && r < e) {
                this.setBlockStateIf(structureWorldAccess, q, Blocks.AIR.getDefaultState(), o2);
                for (Direction direction : DIRECTIONS) {
                    BlockPos blockPos3 = q.offset(direction);
                    FluidState fluidState = structureWorldAccess.getFluidState(blockPos3);
                    if (fluidState.isEmpty()) continue;
                    structureWorldAccess.getFluidTickScheduler().schedule(blockPos3, fluidState.getFluid(), 0);
                }
                continue;
            }
            if (r >= e) {
                this.setBlockStateIf(structureWorldAccess, q, geodeLayerConfig.fillingProvider.getBlockState(random, q), o2);
                continue;
            }
            if (r >= f) {
                boolean bl2;
                boolean bl3 = bl2 = (double)random.nextFloat() < geodeFeatureConfig.useAlternateLayer0Chance;
                if (bl2) {
                    this.setBlockStateIf(structureWorldAccess, q, geodeLayerConfig.alternateInnerLayerProvider.getBlockState(random, q), o2);
                } else {
                    this.setBlockStateIf(structureWorldAccess, q, geodeLayerConfig.innerLayerProvider.getBlockState(random, q), o2);
                }
                if (geodeFeatureConfig.placementsRequireLayer0Alternate && !bl2 || !((double)random.nextFloat() < geodeFeatureConfig.usePotentialPlacementsChance)) continue;
                n2.add(q.toImmutable());
                continue;
            }
            if (r >= g) {
                this.setBlockStateIf(structureWorldAccess, q, geodeLayerConfig.middleLayerProvider.getBlockState(random, q), o2);
                continue;
            }
            if (!(r >= h)) continue;
            this.setBlockStateIf(structureWorldAccess, q, geodeLayerConfig.outerLayerProvider.getBlockState(random, q), o2);
        }
        List<BlockState> p = geodeLayerConfig.innerBlocks;
        block5: for (BlockPos blockPos2 : n2) {
            blockState = (BlockState)Util.getRandom(p, random);
            for (Direction direction2 : DIRECTIONS) {
                if (blockState.contains(Properties.FACING)) {
                    blockState = (BlockState)blockState.with(Properties.FACING, direction2);
                }
                BlockPos bl2 = blockPos2.offset(direction2);
                BlockState pair = structureWorldAccess.getBlockState(bl2);
                if (blockState.contains(Properties.WATERLOGGED)) {
                    blockState = (BlockState)blockState.with(Properties.WATERLOGGED, pair.getFluidState().isStill());
                }
                if (!BuddingAmethystBlock.canGrowIn(pair)) continue;
                this.setBlockStateIf(structureWorldAccess, bl2, blockState, o2);
                continue block5;
            }
        }
        return true;
    }
}

