/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityInteraction;

public interface InteractionObserver {
    public void onInteractionWith(EntityInteraction var1, Entity var2);
}

