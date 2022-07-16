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
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class ForkingTrunkPlacer
extends TrunkPlacer {
    public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> ForkingTrunkPlacer.fillTrunkPlacerFields(instance).apply(instance, ForkingTrunkPlacer::new));

    public ForkingTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.FORKING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        int o;
        ForkingTrunkPlacer.setToDirt(world, replacer, random, startPos.down(), config);
        ArrayList<FoliagePlacer.TreeNode> list = Lists.newArrayList();
        Direction direction = Direction.Type.HORIZONTAL.random(random);
        int i = height - random.nextInt(4) - 1;
        int j = 3 - random.nextInt(3);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int k = startPos.getX();
        int l = startPos.getZ();
        int m = 0;
        for (int n = 0; n < height; ++n) {
            o = startPos.getY() + n;
            if (n >= i && j > 0) {
                k += direction.getOffsetX();
                l += direction.getOffsetZ();
                --j;
            }
            if (!ForkingTrunkPlacer.getAndSetState(world, replacer, random, mutable.set(k, o, l), config)) continue;
            m = o + 1;
        }
        list.add(new FoliagePlacer.TreeNode(new BlockPos(k, m, l), 1, false));
        k = startPos.getX();
        l = startPos.getZ();
        Direction n = Direction.Type.HORIZONTAL.random(random);
        if (n != direction) {
            o = i - random.nextInt(2) - 1;
            int p = 1 + random.nextInt(3);
            m = 0;
            for (int q = o; q < height && p > 0; ++q, --p) {
                if (q < 1) continue;
                int r = startPos.getY() + q;
                if (!ForkingTrunkPlacer.getAndSetState(world, replacer, random, mutable.set(k += n.getOffsetX(), r, l += n.getOffsetZ()), config)) continue;
                m = r + 1;
            }
            if (m > 1) {
                list.add(new FoliagePlacer.TreeNode(new BlockPos(k, m, l), 0, false));
            }
        }
        return list;
    }
}

