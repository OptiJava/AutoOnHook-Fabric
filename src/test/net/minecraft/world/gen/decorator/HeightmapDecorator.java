/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorContext;
import net.minecraft.world.gen.decorator.HeightmapDecoratorConfig;

public class HeightmapDecorator
extends Decorator<HeightmapDecoratorConfig> {
    public HeightmapDecorator(Codec<HeightmapDecoratorConfig> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecoratorContext decoratorContext, Random random, HeightmapDecoratorConfig heightmapDecoratorConfig, BlockPos blockPos) {
        int j;
        int i = blockPos.getX();
        int k = decoratorContext.getTopY(heightmapDecoratorConfig.heightmap, i, j = blockPos.getZ());
        if (k > decoratorContext.getBottomY()) {
            return Stream.of(new BlockPos(i, k, j));
        }
        return Stream.of(new BlockPos[0]);
    }
}

