/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

public class PiglinSpecificSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.MOBS, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, new MemoryModuleType[]{MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT});
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        brain.remember(MemoryModuleType.NEAREST_REPELLENT, PiglinSpecificSensor.findPiglinRepellent(world, entity));
        Optional<Object> optional = Optional.empty();
        Optional<Object> optional2 = Optional.empty();
        Optional<Object> optional3 = Optional.empty();
        Optional<Object> optional4 = Optional.empty();
        Optional<Object> optional5 = Optional.empty();
        Optional<Object> optional6 = Optional.empty();
        Optional<Object> optional7 = Optional.empty();
        int i = 0;
        ArrayList<LivingEntity> list = Lists.newArrayList();
        ArrayList<AbstractPiglinEntity> list2 = Lists.newArrayList();
        List list3 = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of());
        for (LivingEntity livingEntity : list3) {
            LivingEntity hoglinEntity;
            if (livingEntity instanceof HoglinEntity) {
                hoglinEntity = (HoglinEntity)livingEntity;
                if (((PassiveEntity)hoglinEntity).isBaby() && !optional3.isPresent()) {
                    optional3 = Optional.of(hoglinEntity);
                    continue;
                }
                if (!((HoglinEntity)hoglinEntity).isAdult()) continue;
                ++i;
                if (optional2.isPresent() || !((HoglinEntity)hoglinEntity).canBeHunted()) continue;
                optional2 = Optional.of(hoglinEntity);
                continue;
            }
            if (livingEntity instanceof PiglinBruteEntity) {
                list.add((PiglinBruteEntity)livingEntity);
                continue;
            }
            if (livingEntity instanceof PiglinEntity) {
                hoglinEntity = (PiglinEntity)livingEntity;
                if (((PiglinEntity)hoglinEntity).isBaby() && !optional4.isPresent()) {
                    optional4 = Optional.of(hoglinEntity);
                    continue;
                }
                if (!((AbstractPiglinEntity)hoglinEntity).isAdult()) continue;
                list.add(hoglinEntity);
                continue;
            }
            if (livingEntity instanceof PlayerEntity) {
                hoglinEntity = (PlayerEntity)livingEntity;
                if (!optional6.isPresent() && entity.canTarget(livingEntity) && !PiglinBrain.wearsGoldArmor(hoglinEntity)) {
                    optional6 = Optional.of(hoglinEntity);
                }
                if (optional7.isPresent() || ((PlayerEntity)hoglinEntity).isSpectator() || !PiglinBrain.isGoldHoldingPlayer(hoglinEntity)) continue;
                optional7 = Optional.of(hoglinEntity);
                continue;
            }
            if (!optional.isPresent() && (livingEntity instanceof WitherSkeletonEntity || livingEntity instanceof WitherEntity)) {
                optional = Optional.of((MobEntity)livingEntity);
                continue;
            }
            if (optional5.isPresent() || !PiglinBrain.isZombified(livingEntity.getType())) continue;
            optional5 = Optional.of(livingEntity);
        }
        List list4 = brain.getOptionalMemory(MemoryModuleType.MOBS).orElse(ImmutableList.of());
        for (LivingEntity hoglinEntity : list4) {
            if (!(hoglinEntity instanceof AbstractPiglinEntity) || !((AbstractPiglinEntity)hoglinEntity).isAdult()) continue;
            list2.add((AbstractPiglinEntity)hoglinEntity);
        }
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional5);
        brain.remember(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
        brain.remember(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
        brain.remember(MemoryModuleType.NEARBY_ADULT_PIGLINS, list2);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, list);
        brain.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, list.size());
        brain.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
    }

    private static Optional<BlockPos> findPiglinRepellent(ServerWorld world, LivingEntity entity) {
        return BlockPos.findClosest(entity.getBlockPos(), 8, 4, pos -> PiglinSpecificSensor.isPiglinRepellent(world, pos));
    }

    private static boolean isPiglinRepellent(ServerWorld world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        boolean bl = blockState.isIn(BlockTags.PIGLIN_REPELLENTS);
        if (bl && blockState.isOf(Blocks.SOUL_CAMPFIRE)) {
            return CampfireBlock.isLitCampfire(blockState);
        }
        return bl;
    }
}

