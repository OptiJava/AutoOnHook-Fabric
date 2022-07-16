/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RandomPatchFeature
extends Feature<RandomPatchFeatureConfig> {
    public RandomPatchFeature(Codec<RandomPatchFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomPatchFeatureConfig> context) {
        RandomPatchFeatureConfig randomPatchFeatureConfig = context.getConfig();
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockState blockState = randomPatchFeatureConfig.stateProvider.getBlockState(random, blockPos);
        BlockPos blockPos2 = randomPatchFeatureConfig.project ? structureWorldAccess.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, blockPos) : blockPos;
        int i = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int j = 0; j < randomPatchFeatureConfig.tries; ++j) {
            mutable.set(blockPos2, random.nextInt(randomPatchFeatureConfig.spreadX + 1) - random.nextInt(randomPatchFeatureConfig.spreadX + 1), random.nextInt(randomPatchFeatureConfig.spreadY + 1) - random.nextInt(randomPatchFeatureConfig.spreadY + 1), random.nextInt(randomPatchFeatureConfig.spreadZ + 1) - random.nextInt(randomPatchFeatureConfig.spreadZ + 1));
            Vec3i blockPos3 = mutable.down();
            BlockState blockState2 = structureWorldAccess.getBlockState((BlockPos)blockPos3);
            if (!structureWorldAccess.isAir(mutable) && (!randomPatchFeatureConfig.canReplace || !structureWorldAccess.getBlockState(mutable).getMaterial().isReplaceable()) || !blockState.canPlaceAt(structureWorldAccess, mutable) || !randomPatchFeatureConfig.whitelist.isEmpty() && !randomPatchFeatureConfig.whitelist.contains(blockState2.getBlock()) || randomPatchFeatureConfig.blacklist.contains(blockState2) || randomPatchFeatureConfig.needsWater && !structureWorldAccess.getFluidState(((BlockPos)blockPos3).west()).isIn(FluidTags.WATER) && !structureWorldAccess.getFluidState(((BlockPos)blockPos3).east()).isIn(FluidTags.WATER) && !structureWorldAccess.getFluidState(((BlockPos)blockPos3).north()).isIn(FluidTags.WATER) && !structureWorldAccess.getFluidState(((BlockPos)blockPos3).south()).isIn(FluidTags.WATER)) continue;
            randomPatchFeatureConfig.blockPlacer.generate(structureWorldAccess, mutable, blockState, random);
            ++i;
        }
        return i > 0;
    }
}

