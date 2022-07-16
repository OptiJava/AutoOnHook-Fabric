/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity;

import net.minecraft.entity.Mount;

public interface JumpingMount
extends Mount {
    public void setJumpStrength(int var1);

    public boolean canJump();

    public void startJumping(int var1);

    public void stopJumping();
}

