/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen;

/**
 * A property delegate represents an indexed list of integer properties.
 * 
 * <p>Property delegates are used for displaying integer values in screens,
 * such as the progress bars in furnaces.
 */
public interface PropertyDelegate {
    public int get(int var1);

    public void set(int var1, int var2);

    public int size();
}

