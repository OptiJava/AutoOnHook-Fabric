/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.sapling;

import java.util.Random;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.Nullable;

public class OakSaplingGenerator
extends SaplingGenerator {
    @Override
    @Nullable
    protected ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random random, boolean bees) {
        if (random.nextInt(10) == 0) {
            return bees ? ConfiguredFeatures.FANCY_OAK_BEES_005 : ConfiguredFeatures.FANCY_OAK;
        }
        return bees ? ConfiguredFeatures.OAK_BEES_005 : ConfiguredFeatures.OAK;
    }
}

