/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.snooper;

import net.minecraft.util.snooper.Snooper;

public interface SnooperListener {
    public void addSnooperInfo(Snooper var1);

    public void addInitialSnooperInfo(Snooper var1);

    public boolean isSnooperEnabled();
}

