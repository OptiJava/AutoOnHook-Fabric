/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome.layer.util;

import net.minecraft.world.biome.layer.util.LayerSampler;

public interface LayerFactory<A extends LayerSampler> {
    public A make();
}

