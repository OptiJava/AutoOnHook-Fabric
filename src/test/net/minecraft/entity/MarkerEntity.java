/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

public class MarkerEntity
extends Entity {
    /**
     * The name of the compound tag that stores the marker's custom data.
     */
    private static final String DATA_KEY = "data";
    private NbtCompound data = new NbtCompound();

    public MarkerEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    @Override
    public void tick() {
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.data = nbt.getCompound(DATA_KEY);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put(DATA_KEY, this.data.copy());
    }

    @Override
    public Packet<?> createSpawnPacket() {
        throw new IllegalStateException("Markers should never be sent");
    }

    @Override
    protected void addPassenger(Entity passenger) {
        passenger.stopRiding();
    }
}

