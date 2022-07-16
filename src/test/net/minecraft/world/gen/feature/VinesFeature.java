/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class VinesFeature
extends Feature<DefaultFeatureConfig> {
    public VinesFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        context.getConfig();
        if (!structureWorldAccess.isAir(blockPos)) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || !VineBlock.shouldConnectTo(structureWorldAccess, blockPos.offset(direction), direction)) continue;
            structureWorldAccess.setBlockState(blockPos, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.getFacingProperty(direction), true), Block.NOTIFY_LISTENERS);
            return true;
        }
        return false;
    }
}

