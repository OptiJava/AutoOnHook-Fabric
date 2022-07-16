/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.collection;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.collection.IndexedIterable;
import org.jetbrains.annotations.Nullable;

public class IdList<T>
implements IndexedIterable<T> {
    public static final int NULL_ID = -1;
    private int nextId;
    private final IdentityHashMap<T, Integer> idMap;
    private final List<T> list;

    public IdList() {
        this(512);
    }

    public IdList(int initialSize) {
        this.list = Lists.newArrayListWithExpectedSize(initialSize);
        this.idMap = new IdentityHashMap(initialSize);
    }

    public void set(T value, int id) {
        this.idMap.put(value, id);
        while (this.list.size() <= id) {
            this.list.add(null);
        }
        this.list.set(id, value);
        if (this.nextId <= id) {
            this.nextId = id + 1;
        }
    }

    public void add(T value) {
        this.set(value, this.nextId);
    }

    @Override
    public int getRawId(T entry) {
        Integer integer = this.idMap.get(entry);
        return integer == null ? -1 : integer;
    }

    @Override
    @Nullable
    public final T get(int index) {
        if (index >= 0 && index < this.list.size()) {
            return this.list.get(index);
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.list.iterator(), Predicates.notNull());
    }

    public boolean containsKey(int index) {
        return this.get(index) != null;
    }

    public int size() {
        return this.idMap.size();
    }
}

