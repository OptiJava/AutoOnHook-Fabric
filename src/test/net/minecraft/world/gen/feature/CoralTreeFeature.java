/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.CoralFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

public class CoralTreeFeature
extends CoralFeature {
    public CoralTreeFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    protected boolean generateCoral(WorldAccess world, Random random, BlockPos pos, BlockState state) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        int i = random.nextInt(3) + 1;
        for (int j = 0; j < i; ++j) {
            if (!this.generateCoralPiece(world, random, mutable, state)) {
                return true;
            }
            mutable.move(Direction.UP);
        }
        BlockPos j = mutable.toImmutable();
        int k = random.nextInt(3) + 2;
        ArrayList<Direction> list = Lists.newArrayList(Direction.Type.HORIZONTAL);
        Collections.shuffle(list, random);
        List list2 = list.subList(0, k);
        for (Direction direction : list2) {
            mutable.set(j);
            mutable.move(direction);
            int l = random.nextInt(5) + 2;
            int m = 0;
            for (int n = 0; n < l && this.generateCoralPiece(world, random, mutable, state); ++n) {
                mutable.move(Direction.UP);
                if (n != 0 && (++m < 2 || !(random.nextFloat() < 0.25f))) continue;
                mutable.move(direction);
                m = 0;
            }
        }
        return true;
    }
}

