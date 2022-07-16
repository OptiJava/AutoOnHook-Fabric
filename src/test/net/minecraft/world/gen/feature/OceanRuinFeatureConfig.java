/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OceanRuinFeature;

public class OceanRuinFeatureConfig
implements FeatureConfig {
    public static final Codec<OceanRuinFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)OceanRuinFeature.BiomeType.CODEC.fieldOf("biome_temp")).forGetter(oceanRuinFeatureConfig -> oceanRuinFeatureConfig.biomeType), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("large_probability")).forGetter(oceanRuinFeatureConfig -> Float.valueOf(oceanRuinFeatureConfig.largeProbability)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("cluster_probability")).forGetter(oceanRuinFeatureConfig -> Float.valueOf(oceanRuinFeatureConfig.clusterProbability))).apply((Applicative<OceanRuinFeatureConfig, ?>)instance, OceanRuinFeatureConfig::new));
    public final OceanRuinFeature.BiomeType biomeType;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinFeatureConfig(OceanRuinFeature.BiomeType biomeType, float largeProbability, float clusterProbability) {
        this.biomeType = biomeType;
        this.largeProbability = largeProbability;
        this.clusterProbability = clusterProbability;
    }
}

