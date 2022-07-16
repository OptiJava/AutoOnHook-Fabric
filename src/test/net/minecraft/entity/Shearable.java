/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity;

import net.minecraft.sound.SoundCategory;

/**
 * Represents an entity that can be sheared, either by a player or a
 * dispenser.
 */
public interface Shearable {
    public void sheared(SoundCategory var1);

    public boolean isShearable();
}

