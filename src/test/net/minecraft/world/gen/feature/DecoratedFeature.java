/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.DecoratorContext;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class DecoratedFeature
extends Feature<DecoratedFeatureConfig> {
    public DecoratedFeature(Codec<DecoratedFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DecoratedFeatureConfig> context) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        DecoratedFeatureConfig decoratedFeatureConfig = context.getConfig();
        ChunkGenerator chunkGenerator = context.getGenerator();
        Random random = context.getRandom();
        BlockPos blockPos2 = context.getOrigin();
        ConfiguredFeature<?, ?> configuredFeature = decoratedFeatureConfig.feature.get();
        decoratedFeatureConfig.decorator.getPositions(new DecoratorContext(structureWorldAccess, chunkGenerator), random, blockPos2).forEach(blockPos -> {
            if (configuredFeature.generate(structureWorldAccess, chunkGenerator, random, (BlockPos)blockPos)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.isTrue();
    }

    public String toString() {
        return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getId(this));
    }
}

