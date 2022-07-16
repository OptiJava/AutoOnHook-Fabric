/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityChangeListener;

/**
 * A prototype of entity that's suitable for entity manager to handle.
 */
public interface EntityLike {
    /**
     * {@return the network ID of this entity}
     * 
     * <p>Compared to the {@linkplain #getUuid() UUID}, the integer network ID is
     * significantly smaller and more suitable for network transportation. However, it
     * is not persistent across game runs. For persistent purposes such as commands
     * or game data, use the UUID.
     */
    public int getId();

    public UUID getUuid();

    public BlockPos getBlockPos();

    public Box getBoundingBox();

    public void setListener(EntityChangeListener var1);

    /**
     * Returns a stream consisting of this entity and its passengers recursively.
     * Each entity will appear before any of its passengers.
     * 
     * <p>This may be less costly than {@link #streamPassengersAndSelf()} if the
     * stream's iteration would terminates fast, such as finding an arbitrary
     * match of entity in the passengers tree.
     * 
     * @implNote The default implementation is not very efficient.
     * 
     * @see #streamPassengersAndSelf()
     */
    public Stream<? extends EntityLike> streamSelfAndPassengers();

    /**
     * Returns a stream consisting of this entity and its passengers in which
     * this entity's passengers are iterated before this entity.
     * 
     * <p>Moreover, this stream guarantees that any entity only appears after
     * all its passengers have appeared in the stream. This is useful for
     * certain actions that must be applied on passengers before applying on
     * this entity.
     * 
     * @implNote The default implementation is very costly.
     * 
     * @see #streamSelfAndPassengers()
     */
    public Stream<? extends EntityLike> streamPassengersAndSelf();

    public void setRemoved(Entity.RemovalReason var1);

    public boolean shouldSave();

    public boolean isPlayer();
}

