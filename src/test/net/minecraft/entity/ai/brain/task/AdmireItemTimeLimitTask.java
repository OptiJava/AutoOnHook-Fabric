/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.server.world.ServerWorld;

public class AdmireItemTimeLimitTask<E extends PiglinEntity>
extends Task<E> {
    private final int timeLimit;
    private final int cooldown;

    public AdmireItemTimeLimitTask(int timeLimit, int cooldown) {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleState.REGISTERED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleState.REGISTERED));
        this.timeLimit = timeLimit;
        this.cooldown = cooldown;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, E piglinEntity) {
        return ((LivingEntity)piglinEntity).getOffHandStack().isEmpty();
    }

    @Override
    protected void run(ServerWorld serverWorld, E piglinEntity, long l) {
        Brain<PiglinEntity> brain = ((PiglinEntity)piglinEntity).getBrain();
        Optional<Integer> optional = brain.getOptionalMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
        if (!optional.isPresent()) {
            brain.remember(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, 0);
        } else {
            int i = optional.get();
            if (i > this.timeLimit) {
                brain.forget(MemoryModuleType.ADMIRING_ITEM);
                brain.forget(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
                brain.remember(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, true, this.cooldown);
            } else {
                brain.remember(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, i + 1);
            }
        }
    }
}

