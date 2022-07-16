/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.decorator.AbstractCountDecorator;
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig;

public class ChanceDecorator
extends AbstractCountDecorator<ChanceDecoratorConfig> {
    public ChanceDecorator(Codec<ChanceDecoratorConfig> codec) {
        super(codec);
    }

    @Override
    protected int getCount(Random random, ChanceDecoratorConfig chanceDecoratorConfig, BlockPos blockPos) {
        if (random.nextFloat() < 1.0f / (float)chanceDecoratorConfig.chance) {
            return 1;
        }
        return 0;
    }
}

