/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public interface Drawable {
    public void render(MatrixStack var1, int var2, int var3, float var4);
}

