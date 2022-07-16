/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.BlockSource;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class LakeFeature
extends Feature<SingleStateFeatureConfig> {
    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();

    public LakeFeature(Codec<SingleStateFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SingleStateFeatureConfig> context) {
        int s;
        int j;
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        SingleStateFeatureConfig singleStateFeatureConfig = context.getConfig();
        while (blockPos.getY() > structureWorldAccess.getBottomY() + 5 && structureWorldAccess.isAir(blockPos)) {
            blockPos = blockPos.down();
        }
        if (blockPos.getY() <= structureWorldAccess.getBottomY() + 4) {
            return false;
        }
        if (structureWorldAccess.getStructures(ChunkSectionPos.from(blockPos = blockPos.down(4)), StructureFeature.VILLAGE).findAny().isPresent()) {
            return false;
        }
        boolean[] bls = new boolean[2048];
        int i = random.nextInt(4) + 4;
        for (j = 0; j < i; ++j) {
            double d = random.nextDouble() * 6.0 + 3.0;
            double e = random.nextDouble() * 4.0 + 2.0;
            double f = random.nextDouble() * 6.0 + 3.0;
            double g = random.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0;
            double h = random.nextDouble() * (8.0 - e - 4.0) + 2.0 + e / 2.0;
            double k = random.nextDouble() * (16.0 - f - 2.0) + 1.0 + f / 2.0;
            for (int l = 1; l < 15; ++l) {
                for (int m = 1; m < 15; ++m) {
                    for (int n = 1; n < 7; ++n) {
                        double o = ((double)l - g) / (d / 2.0);
                        double p = ((double)n - h) / (e / 2.0);
                        double q = ((double)m - k) / (f / 2.0);
                        double r = o * o + p * p + q * q;
                        if (!(r < 1.0)) continue;
                        bls[(l * 16 + m) * 8 + n] = true;
                    }
                }
            }
        }
        for (j = 0; j < 16; ++j) {
            for (int d = 0; d < 16; ++d) {
                for (s = 0; s < 8; ++s) {
                    boolean e;
                    boolean bl = e = !bls[(j * 16 + d) * 8 + s] && (j < 15 && bls[((j + 1) * 16 + d) * 8 + s] || j > 0 && bls[((j - 1) * 16 + d) * 8 + s] || d < 15 && bls[(j * 16 + d + 1) * 8 + s] || d > 0 && bls[(j * 16 + (d - 1)) * 8 + s] || s < 7 && bls[(j * 16 + d) * 8 + s + 1] || s > 0 && bls[(j * 16 + d) * 8 + (s - 1)]);
                    if (!e) continue;
                    Material material = structureWorldAccess.getBlockState(blockPos.add(j, s, d)).getMaterial();
                    if (s >= 4 && material.isLiquid()) {
                        return false;
                    }
                    if (s >= 4 || material.isSolid() || structureWorldAccess.getBlockState(blockPos.add(j, s, d)) == singleStateFeatureConfig.state) continue;
                    return false;
                }
            }
        }
        for (j = 0; j < 16; ++j) {
            for (int d = 0; d < 16; ++d) {
                for (s = 0; s < 8; ++s) {
                    if (!bls[(j * 16 + d) * 8 + s]) continue;
                    BlockPos e = blockPos.add(j, s, d);
                    boolean material = s >= 4;
                    structureWorldAccess.setBlockState(e, material ? CAVE_AIR : singleStateFeatureConfig.state, Block.NOTIFY_LISTENERS);
                    if (!material) continue;
                    structureWorldAccess.getBlockTickScheduler().schedule(e, CAVE_AIR.getBlock(), 0);
                    this.markBlocksAboveForPostProcessing(structureWorldAccess, e);
                }
            }
        }
        for (j = 0; j < 16; ++j) {
            for (int d = 0; d < 16; ++d) {
                for (s = 4; s < 8; ++s) {
                    BlockPos e;
                    if (!bls[(j * 16 + d) * 8 + s] || !LakeFeature.isSoil(structureWorldAccess.getBlockState(e = blockPos.add(j, s - 1, d))) || structureWorldAccess.getLightLevel(LightType.SKY, blockPos.add(j, s, d)) <= 0) continue;
                    Biome material = structureWorldAccess.getBiome(e);
                    if (material.getGenerationSettings().getSurfaceConfig().getTopMaterial().isOf(Blocks.MYCELIUM)) {
                        structureWorldAccess.setBlockState(e, Blocks.MYCELIUM.getDefaultState(), Block.NOTIFY_LISTENERS);
                        continue;
                    }
                    structureWorldAccess.setBlockState(e, Blocks.GRASS_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }
        if (singleStateFeatureConfig.state.getMaterial() == Material.LAVA) {
            BlockSource j2 = context.getGenerator().getBlockSource();
            for (int d = 0; d < 16; ++d) {
                for (s = 0; s < 16; ++s) {
                    for (int e = 0; e < 8; ++e) {
                        BlockState f;
                        boolean material;
                        boolean bl = material = !bls[(d * 16 + s) * 8 + e] && (d < 15 && bls[((d + 1) * 16 + s) * 8 + e] || d > 0 && bls[((d - 1) * 16 + s) * 8 + e] || s < 15 && bls[(d * 16 + s + 1) * 8 + e] || s > 0 && bls[(d * 16 + (s - 1)) * 8 + e] || e < 7 && bls[(d * 16 + s) * 8 + e + 1] || e > 0 && bls[(d * 16 + s) * 8 + (e - 1)]);
                        if (!material || e >= 4 && random.nextInt(2) == 0 || !(f = structureWorldAccess.getBlockState(blockPos.add(d, e, s))).getMaterial().isSolid() || f.isIn(BlockTags.LAVA_POOL_STONE_REPLACEABLES)) continue;
                        BlockPos blockPos2 = blockPos.add(d, e, s);
                        structureWorldAccess.setBlockState(blockPos2, j2.get(blockPos2), Block.NOTIFY_LISTENERS);
                        this.markBlocksAboveForPostProcessing(structureWorldAccess, blockPos2);
                    }
                }
            }
        }
        if (singleStateFeatureConfig.state.getMaterial() == Material.WATER) {
            for (int j3 = 0; j3 < 16; ++j3) {
                for (int d = 0; d < 16; ++d) {
                    s = 4;
                    BlockPos e = blockPos.add(j3, 4, d);
                    if (!structureWorldAccess.getBiome(e).canSetIce(structureWorldAccess, e, false)) continue;
                    structureWorldAccess.setBlockState(e, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }
        return true;
    }
}

