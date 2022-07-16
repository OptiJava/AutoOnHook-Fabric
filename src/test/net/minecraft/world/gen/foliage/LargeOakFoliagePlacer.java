/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public class LargeOakFoliagePlacer
extends BlobFoliagePlacer {
    public static final Codec<LargeOakFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> LargeOakFoliagePlacer.createCodec(instance).apply(instance, LargeOakFoliagePlacer::new));

    public LargeOakFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, int i) {
        super(intProvider, intProvider2, i);
    }

    @Override
    protected FoliagePlacerType<?> getType() {
        return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
    }

    @Override
    protected void generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
        for (int i = offset; i >= offset - foliageHeight; --i) {
            int j = radius + (i == offset || i == offset - foliageHeight ? 0 : 1);
            this.generateSquare(world, replacer, random, config, treeNode.getCenter(), j, i, treeNode.isGiantTrunk());
        }
    }

    @Override
    protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
        return MathHelper.square((float)dx + 0.5f) + MathHelper.square((float)dz + 0.5f) > (float)(radius * radius);
    }
}

