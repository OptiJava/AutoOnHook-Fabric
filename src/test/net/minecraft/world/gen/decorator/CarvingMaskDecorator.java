/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.decorator.CarvingMaskDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorContext;

public class CarvingMaskDecorator
extends Decorator<CarvingMaskDecoratorConfig> {
    public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfig> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecoratorContext decoratorContext, Random random, CarvingMaskDecoratorConfig carvingMaskDecoratorConfig, BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        BitSet bitSet = decoratorContext.getOrCreateCarvingMask(chunkPos, carvingMaskDecoratorConfig.carver);
        return IntStream.range(0, bitSet.length()).filter(bitSet::get).mapToObj(i -> {
            int j = i & 0xF;
            int k = i >> 4 & 0xF;
            int l = i >> 8;
            return new BlockPos(chunkPos.getStartX() + j, l, chunkPos.getStartZ() + k);
        });
    }
}

