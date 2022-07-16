/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;

@Environment(value=EnvType.CLIENT)
public abstract class Overlay
extends DrawableHelper
implements Drawable {
    public boolean pausesGame() {
        return true;
    }
}

