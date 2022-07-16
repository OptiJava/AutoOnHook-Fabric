/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.VegetationPatchFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class VegetationPatchFeature
extends Feature<VegetationPatchFeatureConfig> {
    public VegetationPatchFeature(Codec<VegetationPatchFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<VegetationPatchFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        VegetationPatchFeatureConfig vegetationPatchFeatureConfig = context.getConfig();
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        Predicate<BlockState> predicate = VegetationPatchFeature.getReplaceablePredicate(vegetationPatchFeatureConfig);
        int i = vegetationPatchFeatureConfig.horizontalRadius.get(random) + 1;
        int j = vegetationPatchFeatureConfig.horizontalRadius.get(random) + 1;
        Set<BlockPos> set = this.placeGroundAndGetPositions(structureWorldAccess, vegetationPatchFeatureConfig, random, blockPos, predicate, i, j);
        this.generateVegetation(context, structureWorldAccess, vegetationPatchFeatureConfig, random, set, i, j);
        return !set.isEmpty();
    }

    protected Set<BlockPos> placeGroundAndGetPositions(StructureWorldAccess world, VegetationPatchFeatureConfig config, Random random, BlockPos pos, Predicate<BlockState> replaceable, int radiusX, int radiusZ) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        BlockPos.Mutable mutable2 = mutable.mutableCopy();
        Direction direction = config.surface.getDirection();
        Direction direction2 = direction.getOpposite();
        HashSet<BlockPos> set = new HashSet<BlockPos>();
        for (int i = -radiusX; i <= radiusX; ++i) {
            boolean bl = i == -radiusX || i == radiusX;
            for (int j = -radiusZ; j <= radiusZ; ++j) {
                int k;
                boolean bl5;
                boolean bl2 = j == -radiusZ || j == radiusZ;
                boolean bl3 = bl || bl2;
                boolean bl4 = bl && bl2;
                boolean bl6 = bl5 = bl3 && !bl4;
                if (bl4 || bl5 && (config.extraEdgeColumnChance == 0.0f || random.nextFloat() > config.extraEdgeColumnChance)) continue;
                mutable.set(pos, i, 0, j);
                for (k = 0; world.testBlockState(mutable, AbstractBlock.AbstractBlockState::isAir) && k < config.verticalRange; ++k) {
                    mutable.move(direction);
                }
                for (k = 0; world.testBlockState(mutable, blockState -> !blockState.isAir()) && k < config.verticalRange; ++k) {
                    mutable.move(direction2);
                }
                mutable2.set((Vec3i)mutable, config.surface.getDirection());
                BlockState blockState2 = world.getBlockState(mutable2);
                if (!world.isAir(mutable) || !blockState2.isSideSolidFullSquare(world, mutable2, config.surface.getDirection().getOpposite())) continue;
                int l = config.depth.get(random) + (config.extraBottomBlockChance > 0.0f && random.nextFloat() < config.extraBottomBlockChance ? 1 : 0);
                BlockPos blockPos = mutable2.toImmutable();
                boolean bl62 = this.placeGround(world, config, replaceable, random, mutable2, l);
                if (!bl62) continue;
                set.add(blockPos);
            }
        }
        return set;
    }

    protected void generateVegetation(FeatureContext<VegetationPatchFeatureConfig> context, StructureWorldAccess world, VegetationPatchFeatureConfig config, Random random, Set<BlockPos> positions, int radiusX, int radiusZ) {
        for (BlockPos blockPos : positions) {
            if (!(config.vegetationChance > 0.0f) || !(random.nextFloat() < config.vegetationChance)) continue;
            this.generateVegetationFeature(world, config, context.getGenerator(), random, blockPos);
        }
    }

    protected boolean generateVegetationFeature(StructureWorldAccess world, VegetationPatchFeatureConfig config, ChunkGenerator generator, Random random, BlockPos pos) {
        return config.vegetationFeature.get().generate(world, generator, random, pos.offset(config.surface.getDirection().getOpposite()));
    }

    protected boolean placeGround(StructureWorldAccess world, VegetationPatchFeatureConfig config, Predicate<BlockState> replaceable, Random random, BlockPos.Mutable pos, int depth) {
        for (int i = 0; i < depth; ++i) {
            BlockState blockState2;
            BlockState blockState = config.groundState.getBlockState(random, pos);
            if (blockState.isOf((blockState2 = world.getBlockState(pos)).getBlock())) continue;
            if (!replaceable.test(blockState2)) {
                return i != 0;
            }
            world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
            pos.move(config.surface.getDirection());
        }
        return true;
    }

    private static Predicate<BlockState> getReplaceablePredicate(VegetationPatchFeatureConfig config) {
        Tag<Block> tag = BlockTags.getTagGroup().getTag(config.replaceable);
        return tag == null ? state -> true : state -> state.isIn(tag);
    }
}

