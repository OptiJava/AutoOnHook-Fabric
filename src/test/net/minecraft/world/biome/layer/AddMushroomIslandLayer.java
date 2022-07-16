/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome.layer;

import net.minecraft.world.biome.BiomeIds;
import net.minecraft.world.biome.layer.BiomeLayers;
import net.minecraft.world.biome.layer.type.DiagonalCrossSamplingLayer;
import net.minecraft.world.biome.layer.util.LayerRandomnessSource;

public enum AddMushroomIslandLayer implements DiagonalCrossSamplingLayer
{
    INSTANCE;


    @Override
    public int sample(LayerRandomnessSource context, int sw, int se, int ne, int nw, int center) {
        if (BiomeLayers.isShallowOcean(center) && BiomeLayers.isShallowOcean(nw) && BiomeLayers.isShallowOcean(sw) && BiomeLayers.isShallowOcean(ne) && BiomeLayers.isShallowOcean(se) && context.nextInt(100) == 0) {
            return BiomeIds.MUSHROOM_FIELDS;
        }
        return center;
    }
}

