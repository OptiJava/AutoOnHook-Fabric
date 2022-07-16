/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity;

import java.util.UUID;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface Tameable {
    @Nullable
    public UUID getOwnerUuid();

    @Nullable
    public Entity getOwner();
}

