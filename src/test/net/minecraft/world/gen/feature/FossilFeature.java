/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FossilFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.apache.commons.lang3.mutable.MutableInt;

public class FossilFeature
extends Feature<FossilFeatureConfig> {
    public FossilFeature(Codec<FossilFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<FossilFeatureConfig> context) {
        int m;
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        BlockRotation blockRotation = BlockRotation.random(random);
        FossilFeatureConfig fossilFeatureConfig = context.getConfig();
        int i = random.nextInt(fossilFeatureConfig.fossilStructures.size());
        StructureManager structureManager = structureWorldAccess.toServerWorld().getServer().getStructureManager();
        Structure structure = structureManager.getStructureOrBlank(fossilFeatureConfig.fossilStructures.get(i));
        Structure structure2 = structureManager.getStructureOrBlank(fossilFeatureConfig.overlayStructures.get(i));
        ChunkPos chunkPos = new ChunkPos(blockPos);
        BlockBox blockBox = new BlockBox(chunkPos.getStartX(), structureWorldAccess.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), structureWorldAccess.getTopY(), chunkPos.getEndZ());
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(blockRotation).setBoundingBox(blockBox).setRandom(random);
        Vec3i vec3i = structure.getRotatedSize(blockRotation);
        int j = random.nextInt(16 - vec3i.getX());
        int k = random.nextInt(16 - vec3i.getZ());
        int l = structureWorldAccess.getTopY();
        for (m = 0; m < vec3i.getX(); ++m) {
            for (int n = 0; n < vec3i.getZ(); ++n) {
                l = Math.min(l, structureWorldAccess.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, blockPos.getX() + m + j, blockPos.getZ() + n + k));
            }
        }
        m = Math.max(l - 15 - random.nextInt(10), structureWorldAccess.getBottomY() + 10);
        BlockPos n = structure.offsetByTransformedSize(blockPos.add(j, 0, k).withY(m), BlockMirror.NONE, blockRotation);
        if (FossilFeature.getEmptyCorners(structureWorldAccess, structure.calculateBoundingBox(structurePlacementData, n)) > fossilFeatureConfig.maxEmptyCorners) {
            return false;
        }
        structurePlacementData.clearProcessors();
        fossilFeatureConfig.fossilProcessors.get().getList().forEach(structureProcessor -> structurePlacementData.addProcessor((StructureProcessor)structureProcessor));
        structure.place(structureWorldAccess, n, n, structurePlacementData, random, Block.NO_REDRAW);
        structurePlacementData.clearProcessors();
        fossilFeatureConfig.overlayProcessors.get().getList().forEach(structureProcessor -> structurePlacementData.addProcessor((StructureProcessor)structureProcessor));
        structure2.place(structureWorldAccess, n, n, structurePlacementData, random, Block.NO_REDRAW);
        return true;
    }

    private static int getEmptyCorners(StructureWorldAccess world, BlockBox box) {
        MutableInt mutableInt = new MutableInt(0);
        box.forEachVertex(blockPos -> {
            BlockState blockState = world.getBlockState((BlockPos)blockPos);
            if (blockState.isAir() || blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.WATER)) {
                mutableInt.add(1);
            }
        });
        return mutableInt.getValue();
    }
}
