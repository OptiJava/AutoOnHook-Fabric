/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SpringFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SpringFeature
extends Feature<SpringFeatureConfig> {
    public SpringFeature(Codec<SpringFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SpringFeatureConfig> context) {
        BlockPos blockPos;
        SpringFeatureConfig springFeatureConfig = context.getConfig();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        if (!springFeatureConfig.validBlocks.contains(structureWorldAccess.getBlockState((blockPos = context.getOrigin()).up()).getBlock())) {
            return false;
        }
        if (springFeatureConfig.requiresBlockBelow && !springFeatureConfig.validBlocks.contains(structureWorldAccess.getBlockState(blockPos.down()).getBlock())) {
            return false;
        }
        BlockState blockState = structureWorldAccess.getBlockState(blockPos);
        if (!blockState.isAir() && !springFeatureConfig.validBlocks.contains(blockState.getBlock())) {
            return false;
        }
        int i = 0;
        int j = 0;
        if (springFeatureConfig.validBlocks.contains(structureWorldAccess.getBlockState(blockPos.west()).getBlock())) {
            ++j;
        }
        if (springFeatureConfig.validBlocks.contains(structureWorldAccess.getBlockState(blockPos.east()).getBlock())) {
            ++j;
        }
        if (springFeatureConfig.validBlocks.contains(structureWorldAccess.getBlockState(blockPos.north()).getBlock())) {
            ++j;
        }
        if (springFeatureConfig.validBlocks.contains(structureWorldAccess.getBlockState(blockPos.south()).getBlock())) {
            ++j;
        }
        if (springFeatureConfig.validBlocks.contains(structureWorldAccess.getBlockState(blockPos.down()).getBlock())) {
            ++j;
        }
        int k = 0;
        if (structureWorldAccess.isAir(blockPos.west())) {
            ++k;
        }
        if (structureWorldAccess.isAir(blockPos.east())) {
            ++k;
        }
        if (structureWorldAccess.isAir(blockPos.north())) {
            ++k;
        }
        if (structureWorldAccess.isAir(blockPos.south())) {
            ++k;
        }
        if (structureWorldAccess.isAir(blockPos.down())) {
            ++k;
        }
        if (j == springFeatureConfig.rockCount && k == springFeatureConfig.holeCount) {
            structureWorldAccess.setBlockState(blockPos, springFeatureConfig.state.getBlockState(), Block.NOTIFY_LISTENERS);
            structureWorldAccess.getFluidTickScheduler().schedule(blockPos, springFeatureConfig.state.getFluid(), 0);
            ++i;
        }
        return i > 0;
    }
}
