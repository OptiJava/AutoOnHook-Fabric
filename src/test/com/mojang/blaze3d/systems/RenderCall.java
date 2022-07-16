/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package com.mojang.blaze3d.systems;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface RenderCall {
    public void execute();
}

