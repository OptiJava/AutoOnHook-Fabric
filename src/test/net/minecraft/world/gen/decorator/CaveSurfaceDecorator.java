/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.world.gen.decorator.CaveSurfaceDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorContext;
import net.minecraft.world.gen.feature.util.CaveSurface;

public class CaveSurfaceDecorator
extends Decorator<CaveSurfaceDecoratorConfig> {
    public CaveSurfaceDecorator(Codec<CaveSurfaceDecoratorConfig> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecoratorContext decoratorContext, Random random, CaveSurfaceDecoratorConfig caveSurfaceDecoratorConfig, BlockPos blockPos) {
        OptionalInt optionalInt;
        Optional<CaveSurface> optional = CaveSurface.create(decoratorContext.getWorld(), blockPos, caveSurfaceDecoratorConfig.searchRange, AbstractBlock.AbstractBlockState::isAir, blockState -> blockState.getMaterial().isSolid());
        if (!optional.isPresent()) {
            return Stream.of(new BlockPos[0]);
        }
        OptionalInt optionalInt2 = optionalInt = caveSurfaceDecoratorConfig.surface == VerticalSurfaceType.CEILING ? optional.get().getCeilingHeight() : optional.get().getFloorHeight();
        if (!optionalInt.isPresent()) {
            return Stream.of(new BlockPos[0]);
        }
        return Stream.of(blockPos.withY(optionalInt.getAsInt() - caveSurfaceDecoratorConfig.surface.getOffset()));
    }
}

