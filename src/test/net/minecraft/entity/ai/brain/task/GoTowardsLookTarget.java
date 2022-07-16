/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

public class GoTowardsLookTarget
extends Task<LivingEntity> {
    private final Function<LivingEntity, Float> speed;
    private final int completionRange;
    private final Predicate<LivingEntity> predicate;

    public GoTowardsLookTarget(float speed, int completionRange) {
        this((LivingEntity entity) -> true, entity -> Float.valueOf(speed), completionRange);
    }

    public GoTowardsLookTarget(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> speed, int completionRange) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT));
        this.speed = speed;
        this.completionRange = completionRange;
        this.predicate = predicate;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
        return this.predicate.test(entity);
    }

    @Override
    protected void run(ServerWorld world, LivingEntity entity, long time) {
        Brain<?> brain = entity.getBrain();
        LookTarget lookTarget = brain.getOptionalMemory(MemoryModuleType.LOOK_TARGET).get();
        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(lookTarget, this.speed.apply(entity).floatValue(), this.completionRange));
    }
}

