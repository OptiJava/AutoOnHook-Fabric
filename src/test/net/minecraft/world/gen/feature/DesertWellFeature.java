/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DesertWellFeature
extends Feature<DefaultFeatureConfig> {
    private static final BlockStatePredicate CAN_GENERATE = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState slab = Blocks.SANDSTONE_SLAB.getDefaultState();
    private final BlockState wall = Blocks.SANDSTONE.getDefaultState();
    private final BlockState fluidInside = Blocks.WATER.getDefaultState();

    public DesertWellFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int i;
        int j;
        int i2;
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        blockPos = blockPos.up();
        while (structureWorldAccess.isAir(blockPos) && blockPos.getY() > structureWorldAccess.getBottomY() + 2) {
            blockPos = blockPos.down();
        }
        if (!CAN_GENERATE.test(structureWorldAccess.getBlockState(blockPos))) {
            return false;
        }
        for (i2 = -2; i2 <= 2; ++i2) {
            for (j = -2; j <= 2; ++j) {
                if (!structureWorldAccess.isAir(blockPos.add(i2, -1, j)) || !structureWorldAccess.isAir(blockPos.add(i2, -2, j))) continue;
                return false;
            }
        }
        for (i2 = -1; i2 <= 0; ++i2) {
            for (j = -2; j <= 2; ++j) {
                for (int k = -2; k <= 2; ++k) {
                    structureWorldAccess.setBlockState(blockPos.add(j, i2, k), this.wall, Block.NOTIFY_LISTENERS);
                }
            }
        }
        structureWorldAccess.setBlockState(blockPos, this.fluidInside, Block.NOTIFY_LISTENERS);
        for (Direction j2 : Direction.Type.HORIZONTAL) {
            structureWorldAccess.setBlockState(blockPos.offset(j2), this.fluidInside, Block.NOTIFY_LISTENERS);
        }
        for (i = -2; i <= 2; ++i) {
            for (int j3 = -2; j3 <= 2; ++j3) {
                if (i != -2 && i != 2 && j3 != -2 && j3 != 2) continue;
                structureWorldAccess.setBlockState(blockPos.add(i, 1, j3), this.wall, Block.NOTIFY_LISTENERS);
            }
        }
        structureWorldAccess.setBlockState(blockPos.add(2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
        structureWorldAccess.setBlockState(blockPos.add(-2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
        structureWorldAccess.setBlockState(blockPos.add(0, 1, 2), this.slab, Block.NOTIFY_LISTENERS);
        structureWorldAccess.setBlockState(blockPos.add(0, 1, -2), this.slab, Block.NOTIFY_LISTENERS);
        for (i = -1; i <= 1; ++i) {
            for (int j4 = -1; j4 <= 1; ++j4) {
                if (i == 0 && j4 == 0) {
                    structureWorldAccess.setBlockState(blockPos.add(i, 4, j4), this.wall, Block.NOTIFY_LISTENERS);
                    continue;
                }
                structureWorldAccess.setBlockState(blockPos.add(i, 4, j4), this.slab, Block.NOTIFY_LISTENERS);
            }
        }
        for (i = 1; i <= 3; ++i) {
            structureWorldAccess.setBlockState(blockPos.add(-1, i, -1), this.wall, Block.NOTIFY_LISTENERS);
            structureWorldAccess.setBlockState(blockPos.add(-1, i, 1), this.wall, Block.NOTIFY_LISTENERS);
            structureWorldAccess.setBlockState(blockPos.add(1, i, -1), this.wall, Block.NOTIFY_LISTENERS);
            structureWorldAccess.setBlockState(blockPos.add(1, i, 1), this.wall, Block.NOTIFY_LISTENERS);
        }
        return true;
    }
}

