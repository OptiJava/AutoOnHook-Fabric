/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.PanicTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

public class StopPanickingTask
extends Task<VillagerEntity> {
    private static final int MAX_DISTANCE = 36;

    public StopPanickingTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        boolean bl;
        boolean bl2 = bl = PanicTask.wasHurt(villagerEntity) || PanicTask.isHostileNearby(villagerEntity) || StopPanickingTask.wasHurtByNearbyEntity(villagerEntity);
        if (!bl) {
            villagerEntity.getBrain().forget(MemoryModuleType.HURT_BY);
            villagerEntity.getBrain().forget(MemoryModuleType.HURT_BY_ENTITY);
            villagerEntity.getBrain().refreshActivities(serverWorld.getTimeOfDay(), serverWorld.getTime());
        }
    }

    private static boolean wasHurtByNearbyEntity(VillagerEntity entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.HURT_BY_ENTITY).filter(livingEntity -> livingEntity.squaredDistanceTo(entity) <= 36.0).isPresent();
    }
}

