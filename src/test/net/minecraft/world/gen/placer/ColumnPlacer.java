/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.placer;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.placer.BlockPlacer;
import net.minecraft.world.gen.placer.BlockPlacerType;

public class ColumnPlacer
extends BlockPlacer {
    public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)IntProvider.field_33450.fieldOf("size")).forGetter(columnPlacer -> columnPlacer.size)).apply((Applicative<ColumnPlacer, ?>)instance, ColumnPlacer::new));
    private final IntProvider size;

    public ColumnPlacer(IntProvider size) {
        this.size = size;
    }

    @Override
    protected BlockPlacerType<?> getType() {
        return BlockPlacerType.COLUMN_PLACER;
    }

    @Override
    public void generate(WorldAccess world, BlockPos pos, BlockState state, Random random) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        int i = this.size.get(random);
        for (int j = 0; j < i; ++j) {
            world.setBlockState(mutable, state, Block.NOTIFY_LISTENERS);
            mutable.move(Direction.UP);
        }
    }
}

