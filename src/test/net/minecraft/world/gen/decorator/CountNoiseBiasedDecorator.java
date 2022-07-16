/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.decorator.AbstractCountDecorator;
import net.minecraft.world.gen.decorator.CountNoiseBiasedDecoratorConfig;

public class CountNoiseBiasedDecorator
extends AbstractCountDecorator<CountNoiseBiasedDecoratorConfig> {
    public CountNoiseBiasedDecorator(Codec<CountNoiseBiasedDecoratorConfig> codec) {
        super(codec);
    }

    @Override
    protected int getCount(Random random, CountNoiseBiasedDecoratorConfig countNoiseBiasedDecoratorConfig, BlockPos blockPos) {
        double d = Biome.FOLIAGE_NOISE.sample((double)blockPos.getX() / countNoiseBiasedDecoratorConfig.noiseFactor, (double)blockPos.getZ() / countNoiseBiasedDecoratorConfig.noiseFactor, false);
        return (int)Math.ceil((d + countNoiseBiasedDecoratorConfig.noiseOffset) * (double)countNoiseBiasedDecoratorConfig.noiseToCountRatio);
    }
}

