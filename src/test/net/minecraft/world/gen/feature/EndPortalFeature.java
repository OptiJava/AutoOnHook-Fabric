/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndPortalFeature
extends Feature<DefaultFeatureConfig> {
    public static final int field_31503 = 4;
    public static final int field_31504 = 4;
    public static final int field_31505 = 1;
    public static final float field_31506 = 0.5f;
    public static final BlockPos ORIGIN = BlockPos.ORIGIN;
    private final boolean open;

    public EndPortalFeature(boolean open) {
        super(DefaultFeatureConfig.CODEC);
        this.open = open;
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        for (BlockPos blockPos2 : BlockPos.iterate(new BlockPos(blockPos.getX() - 4, blockPos.getY() - 1, blockPos.getZ() - 4), new BlockPos(blockPos.getX() + 4, blockPos.getY() + 32, blockPos.getZ() + 4))) {
            boolean bl = blockPos2.isWithinDistance(blockPos, 2.5);
            if (!bl && !blockPos2.isWithinDistance(blockPos, 3.5)) continue;
            if (blockPos2.getY() < blockPos.getY()) {
                if (bl) {
                    this.setBlockState(structureWorldAccess, blockPos2, Blocks.BEDROCK.getDefaultState());
                    continue;
                }
                if (blockPos2.getY() >= blockPos.getY()) continue;
                this.setBlockState(structureWorldAccess, blockPos2, Blocks.END_STONE.getDefaultState());
                continue;
            }
            if (blockPos2.getY() > blockPos.getY()) {
                this.setBlockState(structureWorldAccess, blockPos2, Blocks.AIR.getDefaultState());
                continue;
            }
            if (!bl) {
                this.setBlockState(structureWorldAccess, blockPos2, Blocks.BEDROCK.getDefaultState());
                continue;
            }
            if (this.open) {
                this.setBlockState(structureWorldAccess, new BlockPos(blockPos2), Blocks.END_PORTAL.getDefaultState());
                continue;
            }
            this.setBlockState(structureWorldAccess, new BlockPos(blockPos2), Blocks.AIR.getDefaultState());
        }
        for (int i = 0; i < 4; ++i) {
            this.setBlockState(structureWorldAccess, blockPos.up(i), Blocks.BEDROCK.getDefaultState());
        }
        BlockPos i = blockPos.up(2);
        for (Direction bl : Direction.Type.HORIZONTAL) {
            this.setBlockState(structureWorldAccess, i.offset(bl), (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, bl));
        }
        return true;
    }
}

