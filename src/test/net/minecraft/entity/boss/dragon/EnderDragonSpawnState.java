/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.boss.dragon;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public enum EnderDragonSpawnState {
    START{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int i, BlockPos pos) {
            BlockPos blockPos = new BlockPos(0, 128, 0);
            for (EndCrystalEntity endCrystalEntity : crystals) {
                endCrystalEntity.setBeamTarget(blockPos);
            }
            fight.setSpawnState(PREPARING_TO_SUMMON_PILLARS);
        }
    }
    ,
    PREPARING_TO_SUMMON_PILLARS{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int i, BlockPos pos) {
            if (i < 100) {
                if (i == 0 || i == 50 || i == 51 || i == 52 || i >= 95) {
                    world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
                }
            } else {
                fight.setSpawnState(SUMMONING_PILLARS);
            }
        }
    }
    ,
    SUMMONING_PILLARS{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int i, BlockPos pos) {
            boolean bl2;
            int j = 40;
            boolean bl = i % 40 == 0;
            boolean bl3 = bl2 = i % 40 == 39;
            if (bl || bl2) {
                int k = i / 40;
                List<EndSpikeFeature.Spike> list = EndSpikeFeature.getSpikes(world);
                if (k < list.size()) {
                    EndSpikeFeature.Spike spike = list.get(k);
                    if (bl) {
                        for (EndCrystalEntity endCrystalEntity : crystals) {
                            endCrystalEntity.setBeamTarget(new BlockPos(spike.getCenterX(), spike.getHeight() + 1, spike.getCenterZ()));
                        }
                    } else {
                        int l = 10;
                        for (BlockPos blockPos : BlockPos.iterate(new BlockPos(spike.getCenterX() - 10, spike.getHeight() - 10, spike.getCenterZ() - 10), new BlockPos(spike.getCenterX() + 10, spike.getHeight() + 10, spike.getCenterZ() + 10))) {
                            world.removeBlock(blockPos, false);
                        }
                        world.createExplosion(null, (float)spike.getCenterX() + 0.5f, spike.getHeight(), (float)spike.getCenterZ() + 0.5f, 5.0f, Explosion.DestructionType.DESTROY);
                        EndSpikeFeatureConfig endCrystalEntity = new EndSpikeFeatureConfig(true, ImmutableList.of(spike), new BlockPos(0, 128, 0));
                        Feature.END_SPIKE.configure(endCrystalEntity).generate(world, world.getChunkManager().getChunkGenerator(), new Random(), new BlockPos(spike.getCenterX(), 45, spike.getCenterZ()));
                    }
                } else if (bl) {
                    fight.setSpawnState(SUMMONING_DRAGON);
                }
            }
        }
    }
    ,
    SUMMONING_DRAGON{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int i, BlockPos pos) {
            if (i >= 100) {
                fight.setSpawnState(END);
                fight.resetEndCrystals();
                for (EndCrystalEntity endCrystalEntity : crystals) {
                    endCrystalEntity.setBeamTarget(null);
                    world.createExplosion(endCrystalEntity, endCrystalEntity.getX(), endCrystalEntity.getY(), endCrystalEntity.getZ(), 6.0f, Explosion.DestructionType.NONE);
                    endCrystalEntity.discard();
                }
            } else if (i >= 80) {
                world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
            } else if (i == 0) {
                for (EndCrystalEntity endCrystalEntity : crystals) {
                    endCrystalEntity.setBeamTarget(new BlockPos(0, 128, 0));
                }
            } else if (i < 5) {
                world.syncWorldEvent(WorldEvents.ENDER_DRAGON_RESURRECTED, new BlockPos(0, 128, 0), 0);
            }
        }
    }
    ,
    END{

        @Override
        public void run(ServerWorld world, EnderDragonFight fight, List<EndCrystalEntity> crystals, int i, BlockPos pos) {
        }
    };


    public abstract void run(ServerWorld var1, EnderDragonFight var2, List<EndCrystalEntity> var3, int var4, BlockPos var5);
}

