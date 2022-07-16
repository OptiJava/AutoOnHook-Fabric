/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.structure.ShipwreckGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ShipwreckFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class ShipwreckFeature
extends StructureFeature<ShipwreckFeatureConfig> {
    public ShipwreckFeature(Codec<ShipwreckFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public StructureFeature.StructureStartFactory<ShipwreckFeatureConfig> getStructureStartFactory() {
        return Start::new;
    }

    public static class Start
    extends StructureStart<ShipwreckFeatureConfig> {
        public Start(StructureFeature<ShipwreckFeatureConfig> structureFeature, ChunkPos chunkPos, int i, long l) {
            super(structureFeature, chunkPos, i, l);
        }

        @Override
        public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkPos chunkPos, Biome biome, ShipwreckFeatureConfig shipwreckFeatureConfig, HeightLimitView heightLimitView) {
            BlockRotation blockRotation = BlockRotation.random(this.random);
            BlockPos blockPos = new BlockPos(chunkPos.getStartX(), 90, chunkPos.getStartZ());
            ShipwreckGenerator.addParts(structureManager, blockPos, blockRotation, this, this.random, shipwreckFeatureConfig);
        }
    }
}

