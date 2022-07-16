/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for looking up entities.
 * 
 * <p>It supports iteration, accessing by ID, or by intersection with boxes.
 * 
 * @param <T> the type of indexed entity
 */
public interface EntityLookup<T extends EntityLike> {
    /**
     * Returns an entity by its network ID, or {@code null} if none is found.
     */
    @Nullable
    public T get(int var1);

    /**
     * Returns an entity by its UUID, or {@code null} if none is found.
     */
    @Nullable
    public T get(UUID var1);

    /**
     * Returns an unmodifiable iterable over all entities in this lookup.
     */
    public Iterable<T> iterate();

    /**
     * Performs an {@code action} on each entity of type {@code U} within this
     * lookup.
     * 
     * @param <U> the type of entity to perform action on
     * 
     * @param filter specifies the desired type of entity
     * @param action the action to perform
     */
    public <U extends T> void forEach(TypeFilter<T, U> var1, Consumer<U> var2);

    public void forEachIntersects(Box var1, Consumer<T> var2);

    public <U extends T> void forEachIntersects(TypeFilter<T, U> var1, Box var2, Consumer<U> var3);
}

