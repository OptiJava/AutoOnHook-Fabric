/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.server.world.ServerWorld;

public class PlayDeadTimerTask
extends Task<AxolotlEntity> {
    public PlayDeadTimerTask() {
        super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleState.VALUE_PRESENT));
    }

    @Override
    protected void run(ServerWorld serverWorld, AxolotlEntity axolotlEntity, long l) {
        Brain<AxolotlEntity> brain = axolotlEntity.getBrain();
        int i = brain.getOptionalMemory(MemoryModuleType.PLAY_DEAD_TICKS).get();
        if (i <= 0) {
            brain.forget(MemoryModuleType.PLAY_DEAD_TICKS);
            brain.forget(MemoryModuleType.HURT_BY_ENTITY);
            brain.resetPossibleActivities();
        } else {
            brain.remember(MemoryModuleType.PLAY_DEAD_TICKS, i - 1);
        }
    }
}

