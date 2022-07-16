/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class GiantTrunkPlacer
extends TrunkPlacer {
    public static final Codec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> GiantTrunkPlacer.fillTrunkPlacerFields(instance).apply(instance, GiantTrunkPlacer::new));

    public GiantTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.GIANT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        BlockPos blockPos = startPos.down();
        GiantTrunkPlacer.setToDirt(world, replacer, random, blockPos, config);
        GiantTrunkPlacer.setToDirt(world, replacer, random, blockPos.east(), config);
        GiantTrunkPlacer.setToDirt(world, replacer, random, blockPos.south(), config);
        GiantTrunkPlacer.setToDirt(world, replacer, random, blockPos.south().east(), config);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int i = 0; i < height; ++i) {
            GiantTrunkPlacer.setLog(world, replacer, random, mutable, config, startPos, 0, i, 0);
            if (i >= height - 1) continue;
            GiantTrunkPlacer.setLog(world, replacer, random, mutable, config, startPos, 1, i, 0);
            GiantTrunkPlacer.setLog(world, replacer, random, mutable, config, startPos, 1, i, 1);
            GiantTrunkPlacer.setLog(world, replacer, random, mutable, config, startPos, 0, i, 1);
        }
        return ImmutableList.of(new FoliagePlacer.TreeNode(startPos.up(height), 0, true));
    }

    private static void setLog(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, BlockPos.Mutable pos, TreeFeatureConfig config, BlockPos startPos, int x, int y, int z) {
        pos.set(startPos, x, y, z);
        GiantTrunkPlacer.trySetState(world, replacer, random, pos, config);
    }
}

