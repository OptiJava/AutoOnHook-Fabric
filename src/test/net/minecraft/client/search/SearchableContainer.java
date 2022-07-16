/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.search;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.search.Searchable;

@Environment(value=EnvType.CLIENT)
public interface SearchableContainer<T>
extends Searchable<T> {
    public void add(T var1);

    public void clear();

    public void reload();
}

