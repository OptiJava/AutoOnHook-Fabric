/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

public class GoToWorkTask
extends Task<VillagerEntity> {
    public GoToWorkTask() {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleState.VALUE_PRESENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        BlockPos blockPos = villagerEntity.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().getPos();
        return blockPos.isWithinDistance(villagerEntity.getPos(), 2.0) || villagerEntity.isNatural();
    }

    @Override
    protected void run(ServerWorld serverWorld2, VillagerEntity villagerEntity, long l) {
        GlobalPos globalPos = villagerEntity.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
        villagerEntity.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
        villagerEntity.getBrain().remember(MemoryModuleType.JOB_SITE, globalPos);
        serverWorld2.sendEntityStatus(villagerEntity, (byte)14);
        if (villagerEntity.getVillagerData().getProfession() != VillagerProfession.NONE) {
            return;
        }
        MinecraftServer minecraftServer = serverWorld2.getServer();
        Optional.ofNullable(minecraftServer.getWorld(globalPos.getDimension())).flatMap(serverWorld -> serverWorld.getPointOfInterestStorage().getType(globalPos.getPos())).flatMap(pointOfInterestType -> Registry.VILLAGER_PROFESSION.stream().filter(villagerProfession -> villagerProfession.getWorkStation() == pointOfInterestType).findFirst()).ifPresent(villagerProfession -> {
            villagerEntity.setVillagerData(villagerEntity.getVillagerData().withProfession((VillagerProfession)villagerProfession));
            villagerEntity.reinitializeBrain(serverWorld2);
        });
    }
}

