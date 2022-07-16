/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen;

import net.minecraft.server.world.ServerWorld;

/**
 * Handles spawning entities in a world.
 * 
 * <p>A spawner is typically used to spawn entities within a special context, such as cats in a village or wandering traders.
 */
public interface Spawner {
    /**
     * Spawns entities into a world.
     * 
     * @return the number of entities spawned
     * 
     * @param spawnMonsters whether monsters should be spawned
     * @param spawnAnimals whether animals should be spawned
     */
    public int spawn(ServerWorld var1, boolean var2, boolean var3);
}

