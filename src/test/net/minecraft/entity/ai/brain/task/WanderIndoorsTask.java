/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class WanderIndoorsTask
extends Task<PathAwareEntity> {
    private final float speed;

    public WanderIndoorsTask(float speed) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
        this.speed = speed;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, PathAwareEntity pathAwareEntity) {
        return !serverWorld.isSkyVisible(pathAwareEntity.getBlockPos());
    }

    @Override
    protected void run(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        BlockPos blockPos2 = pathAwareEntity.getBlockPos();
        List list = BlockPos.stream(blockPos2.add(-1, -1, -1), blockPos2.add(1, 1, 1)).map(BlockPos::toImmutable).collect(Collectors.toList());
        Collections.shuffle(list);
        Optional<BlockPos> optional = list.stream().filter(blockPos -> !serverWorld.isSkyVisible((BlockPos)blockPos)).filter(blockPos -> serverWorld.isTopSolid((BlockPos)blockPos, pathAwareEntity)).filter(blockPos -> serverWorld.isSpaceEmpty(pathAwareEntity)).findFirst();
        optional.ifPresent(blockPos -> pathAwareEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget((BlockPos)blockPos, this.speed, 0)));
    }
}

