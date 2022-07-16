/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.DoubleList;

interface PairList {
    public DoubleList getPairs();

    public boolean forEachPair(Consumer var1);

    public int size();

    public static interface Consumer {
        public boolean merge(int var1, int var2, int var3);
    }
}

