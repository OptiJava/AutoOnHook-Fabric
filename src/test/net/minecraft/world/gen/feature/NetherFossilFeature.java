/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.MarginedStructureStart;
import net.minecraft.structure.NetherFossilGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class NetherFossilFeature
extends StructureFeature<RangeDecoratorConfig> {
    public NetherFossilFeature(Codec<RangeDecoratorConfig> codec) {
        super(codec);
    }

    @Override
    public StructureFeature.StructureStartFactory<RangeDecoratorConfig> getStructureStartFactory() {
        return Start::new;
    }

    public static class Start
    extends MarginedStructureStart<RangeDecoratorConfig> {
        public Start(StructureFeature<RangeDecoratorConfig> structureFeature, ChunkPos chunkPos, int i, long l) {
            super(structureFeature, chunkPos, i, l);
        }

        @Override
        public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkPos chunkPos, Biome biome, RangeDecoratorConfig rangeDecoratorConfig, HeightLimitView heightLimitView) {
            int l;
            int i = chunkPos.getStartX() + this.random.nextInt(16);
            int j = chunkPos.getStartZ() + this.random.nextInt(16);
            int k = chunkGenerator.getSeaLevel();
            HeightContext heightContext = new HeightContext(chunkGenerator, heightLimitView);
            VerticalBlockSample verticalBlockSample = chunkGenerator.getColumnSample(i, j, heightLimitView);
            BlockPos.Mutable mutable = new BlockPos.Mutable(i, l, j);
            for (l = rangeDecoratorConfig.heightProvider.get(this.random, heightContext); l > k; --l) {
                BlockState blockState = verticalBlockSample.getState(mutable);
                mutable.move(Direction.DOWN);
                BlockState blockState2 = verticalBlockSample.getState(mutable);
                if (blockState.isAir() && (blockState2.isOf(Blocks.SOUL_SAND) || blockState2.isSideSolidFullSquare(EmptyBlockView.INSTANCE, mutable, Direction.UP))) break;
            }
            if (l <= k) {
                return;
            }
            NetherFossilGenerator.addPieces(structureManager, this, this.random, new BlockPos(i, l, j));
        }
    }
}

