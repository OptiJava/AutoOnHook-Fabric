/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.structure.StructurePiece;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DungeonFeature
extends Feature<DefaultFeatureConfig> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityType<?>[] MOB_SPAWNER_ENTITIES = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.getDefaultState();

    public DungeonFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        Object material;
        BlockPos blockPos2;
        int u;
        int t;
        int s;
        Predicate<BlockState> predicate = Feature.notInBlockTagPredicate(BlockTags.FEATURES_CANNOT_REPLACE.getId());
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        int i = 3;
        int j = random.nextInt(2) + 2;
        int k = -j - 1;
        int l = j + 1;
        int m = -1;
        int n = 4;
        int o = random.nextInt(2) + 2;
        int p = -o - 1;
        int q = o + 1;
        int r = 0;
        for (s = k; s <= l; ++s) {
            for (t = -1; t <= 4; ++t) {
                for (u = p; u <= q; ++u) {
                    blockPos2 = blockPos.add(s, t, u);
                    material = structureWorldAccess.getBlockState(blockPos2).getMaterial();
                    boolean bl = ((Material)material).isSolid();
                    if (t == -1 && !bl) {
                        return false;
                    }
                    if (t == 4 && !bl) {
                        return false;
                    }
                    if (s != k && s != l && u != p && u != q || t != 0 || !structureWorldAccess.isAir(blockPos2) || !structureWorldAccess.isAir(blockPos2.up())) continue;
                    ++r;
                }
            }
        }
        if (r < 1 || r > 5) {
            return false;
        }
        for (s = k; s <= l; ++s) {
            for (t = 3; t >= -1; --t) {
                for (u = p; u <= q; ++u) {
                    blockPos2 = blockPos.add(s, t, u);
                    material = structureWorldAccess.getBlockState(blockPos2);
                    if (s == k || t == -1 || u == p || s == l || t == 4 || u == q) {
                        if (blockPos2.getY() >= structureWorldAccess.getBottomY() && !structureWorldAccess.getBlockState(blockPos2.down()).getMaterial().isSolid()) {
                            structureWorldAccess.setBlockState(blockPos2, AIR, Block.NOTIFY_LISTENERS);
                            continue;
                        }
                        if (!((AbstractBlock.AbstractBlockState)material).getMaterial().isSolid() || ((AbstractBlock.AbstractBlockState)material).isOf(Blocks.CHEST)) continue;
                        if (t == -1 && random.nextInt(4) != 0) {
                            this.setBlockStateIf(structureWorldAccess, blockPos2, Blocks.MOSSY_COBBLESTONE.getDefaultState(), predicate);
                            continue;
                        }
                        this.setBlockStateIf(structureWorldAccess, blockPos2, Blocks.COBBLESTONE.getDefaultState(), predicate);
                        continue;
                    }
                    if (((AbstractBlock.AbstractBlockState)material).isOf(Blocks.CHEST) || ((AbstractBlock.AbstractBlockState)material).isOf(Blocks.SPAWNER)) continue;
                    this.setBlockStateIf(structureWorldAccess, blockPos2, AIR, predicate);
                }
            }
        }
        block6: for (s = 0; s < 2; ++s) {
            for (t = 0; t < 3; ++t) {
                int material2;
                int blockPos22;
                u = blockPos.getX() + random.nextInt(j * 2 + 1) - j;
                BlockPos bl = new BlockPos(u, blockPos22 = blockPos.getY(), material2 = blockPos.getZ() + random.nextInt(o * 2 + 1) - o);
                if (!structureWorldAccess.isAir(bl)) continue;
                int v = 0;
                for (Direction direction : Direction.Type.HORIZONTAL) {
                    if (!structureWorldAccess.getBlockState(bl.offset(direction)).getMaterial().isSolid()) continue;
                    ++v;
                }
                if (v != 1) continue;
                this.setBlockStateIf(structureWorldAccess, bl, StructurePiece.orientateChest(structureWorldAccess, bl, Blocks.CHEST.getDefaultState()), predicate);
                LootableContainerBlockEntity.setLootTable(structureWorldAccess, random, bl, LootTables.SIMPLE_DUNGEON_CHEST);
                continue block6;
            }
        }
        this.setBlockStateIf(structureWorldAccess, blockPos, Blocks.SPAWNER.getDefaultState(), predicate);
        BlockEntity s2 = structureWorldAccess.getBlockEntity(blockPos);
        if (s2 instanceof MobSpawnerBlockEntity) {
            ((MobSpawnerBlockEntity)s2).getLogic().setEntityId(this.getMobSpawnerEntity(random));
        } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", (Object)blockPos.getX(), (Object)blockPos.getY(), (Object)blockPos.getZ());
        }
        return true;
    }

    private EntityType<?> getMobSpawnerEntity(Random random) {
        return Util.getRandom(MOB_SPAWNER_ENTITIES, random);
    }
}

