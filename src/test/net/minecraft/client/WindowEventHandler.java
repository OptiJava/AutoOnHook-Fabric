/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface WindowEventHandler {
    public void onWindowFocusChanged(boolean var1);

    public void onResolutionChanged();

    public void onCursorEnterChanged();
}

