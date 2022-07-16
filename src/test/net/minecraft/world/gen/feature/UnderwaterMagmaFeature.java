/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.UnderwaterMagmaFeatureConfig;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class UnderwaterMagmaFeature
extends Feature<UnderwaterMagmaFeatureConfig> {
    public UnderwaterMagmaFeature(Codec<UnderwaterMagmaFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<UnderwaterMagmaFeatureConfig> context) {
        Vec3i vec3i;
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        UnderwaterMagmaFeatureConfig underwaterMagmaFeatureConfig = context.getConfig();
        Random random = context.getRandom();
        OptionalInt optionalInt = UnderwaterMagmaFeature.getFloorHeight(structureWorldAccess, blockPos, underwaterMagmaFeatureConfig);
        if (!optionalInt.isPresent()) {
            return false;
        }
        BlockPos blockPos2 = blockPos.withY(optionalInt.getAsInt());
        Box box = new Box(blockPos2.subtract(vec3i = new Vec3i(underwaterMagmaFeatureConfig.placementRadiusAroundFloor, underwaterMagmaFeatureConfig.placementRadiusAroundFloor, underwaterMagmaFeatureConfig.placementRadiusAroundFloor)), blockPos2.add(vec3i));
        return BlockPos.stream(box).filter(pos -> random.nextFloat() < underwaterMagmaFeatureConfig.placementProbabilityPerValidPosition).filter(pos -> this.isValidPosition(structureWorldAccess, (BlockPos)pos)).mapToInt(pos -> {
            structureWorldAccess.setBlockState((BlockPos)pos, Blocks.MAGMA_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
            return 1;
        }).sum() > 0;
    }

    private static OptionalInt getFloorHeight(StructureWorldAccess world, BlockPos pos, UnderwaterMagmaFeatureConfig config) {
        Predicate<BlockState> predicate = state -> state.isOf(Blocks.WATER);
        Predicate<BlockState> predicate2 = state -> !state.isOf(Blocks.WATER);
        Optional<CaveSurface> optional = CaveSurface.create(world, pos, config.floorSearchRange, predicate, predicate2);
        return optional.map(CaveSurface::getFloorHeight).orElseGet(OptionalInt::empty);
    }

    private boolean isValidPosition(StructureWorldAccess world, BlockPos pos) {
        if (this.isWaterOrAir(world, pos) || this.isWaterOrAir(world, pos.down())) {
            return false;
        }
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (!this.isWaterOrAir(world, pos.offset(direction))) continue;
            return false;
        }
        return true;
    }

    private boolean isWaterOrAir(WorldAccess world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isOf(Blocks.WATER) || blockState.isAir();
    }
}

