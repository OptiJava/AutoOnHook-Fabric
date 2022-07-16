/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class DarkOakTrunkPlacer
extends TrunkPlacer {
    public static final Codec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> DarkOakTrunkPlacer.fillTrunkPlacerFields(instance).apply(instance, DarkOakTrunkPlacer::new));

    public DarkOakTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        int r;
        int q;
        ArrayList<FoliagePlacer.TreeNode> list = Lists.newArrayList();
        BlockPos blockPos = startPos.down();
        DarkOakTrunkPlacer.setToDirt(world, replacer, random, blockPos, config);
        DarkOakTrunkPlacer.setToDirt(world, replacer, random, blockPos.east(), config);
        DarkOakTrunkPlacer.setToDirt(world, replacer, random, blockPos.south(), config);
        DarkOakTrunkPlacer.setToDirt(world, replacer, random, blockPos.south().east(), config);
        Direction direction = Direction.Type.HORIZONTAL.random(random);
        int i = height - random.nextInt(4);
        int j = 2 - random.nextInt(3);
        int k = startPos.getX();
        int l = startPos.getY();
        int m = startPos.getZ();
        int n = k;
        int o = m;
        int p = l + height - 1;
        for (q = 0; q < height; ++q) {
            BlockPos blockPos2;
            if (q >= i && j > 0) {
                n += direction.getOffsetX();
                o += direction.getOffsetZ();
                --j;
            }
            if (!TreeFeature.isAirOrLeaves(world, blockPos2 = new BlockPos(n, r = l + q, o))) continue;
            DarkOakTrunkPlacer.getAndSetState(world, replacer, random, blockPos2, config);
            DarkOakTrunkPlacer.getAndSetState(world, replacer, random, blockPos2.east(), config);
            DarkOakTrunkPlacer.getAndSetState(world, replacer, random, blockPos2.south(), config);
            DarkOakTrunkPlacer.getAndSetState(world, replacer, random, blockPos2.east().south(), config);
        }
        list.add(new FoliagePlacer.TreeNode(new BlockPos(n, p, o), 0, true));
        for (q = -1; q <= 2; ++q) {
            for (r = -1; r <= 2; ++r) {
                if (q >= 0 && q <= 1 && r >= 0 && r <= 1 || random.nextInt(3) > 0) continue;
                int blockPos2 = random.nextInt(3) + 2;
                for (int s = 0; s < blockPos2; ++s) {
                    DarkOakTrunkPlacer.getAndSetState(world, replacer, random, new BlockPos(k + q, p - s - 1, m + r), config);
                }
                list.add(new FoliagePlacer.TreeNode(new BlockPos(n + q, p, o + r), 0, false));
            }
        }
        return list;
    }
}

