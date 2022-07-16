/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.entity.SectionedEntityCache;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of entity lookup backed by two separate {@link
 * EntityIndex} and {@link SectionedEntityCache}.
 * 
 * <p>It's up to the user to ensure that the index and the cache are
 * consistent with each other.
 * 
 * @param <T> the type of indexed entity
 */
public class SimpleEntityLookup<T extends EntityLike>
implements EntityLookup<T> {
    private final EntityIndex<T> index;
    private final SectionedEntityCache<T> cache;

    public SimpleEntityLookup(EntityIndex<T> index, SectionedEntityCache<T> cache) {
        this.index = index;
        this.cache = cache;
    }

    @Override
    @Nullable
    public T get(int id) {
        return this.index.get(id);
    }

    @Override
    @Nullable
    public T get(UUID uuid) {
        return this.index.get(uuid);
    }

    @Override
    public Iterable<T> iterate() {
        return this.index.iterate();
    }

    @Override
    public <U extends T> void forEach(TypeFilter<T, U> filter, Consumer<U> action) {
        this.index.forEach(filter, action);
    }

    @Override
    public void forEachIntersects(Box box, Consumer<T> action) {
        this.cache.forEachIntersects(box, action);
    }

    @Override
    public <U extends T> void forEachIntersects(TypeFilter<T, U> filter, Box box, Consumer<U> action) {
        this.cache.forEachIntersects(filter, box, action);
    }
}

