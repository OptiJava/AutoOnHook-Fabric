/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.task.FindWalkTargetTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.raid.Raid;

public class RunAroundAfterRaidTask
extends FindWalkTargetTask {
    public RunAroundAfterRaidTask(float f) {
        super(f);
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, PathAwareEntity pathAwareEntity) {
        Raid raid = serverWorld.getRaidAt(pathAwareEntity.getBlockPos());
        return raid != null && raid.hasWon() && super.shouldRun(serverWorld, pathAwareEntity);
    }
}

