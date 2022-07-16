/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class GlowstoneBlobFeature
extends Feature<DefaultFeatureConfig> {
    public GlowstoneBlobFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        if (!structureWorldAccess.isAir(blockPos)) {
            return false;
        }
        BlockState blockState = structureWorldAccess.getBlockState(blockPos.up());
        if (!(blockState.isOf(Blocks.NETHERRACK) || blockState.isOf(Blocks.BASALT) || blockState.isOf(Blocks.BLACKSTONE))) {
            return false;
        }
        structureWorldAccess.setBlockState(blockPos, Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
        for (int i = 0; i < 1500; ++i) {
            BlockPos blockPos2 = blockPos.add(random.nextInt(8) - random.nextInt(8), -random.nextInt(12), random.nextInt(8) - random.nextInt(8));
            if (!structureWorldAccess.getBlockState(blockPos2).isAir()) continue;
            int j = 0;
            for (Direction direction : Direction.values()) {
                if (structureWorldAccess.getBlockState(blockPos2.offset(direction)).isOf(Blocks.GLOWSTONE)) {
                    ++j;
                }
                if (j > 1) break;
            }
            if (j != true) continue;
            structureWorldAccess.setBlockState(blockPos2, Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        return true;
    }
}

