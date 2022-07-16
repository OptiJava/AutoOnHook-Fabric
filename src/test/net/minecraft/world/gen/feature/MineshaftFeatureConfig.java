/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.MineshaftFeature;

public class MineshaftFeatureConfig
implements FeatureConfig {
    public static final Codec<MineshaftFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(mineshaftFeatureConfig -> Float.valueOf(mineshaftFeatureConfig.probability)), ((MapCodec)MineshaftFeature.Type.CODEC.fieldOf("type")).forGetter(mineshaftFeatureConfig -> mineshaftFeatureConfig.type)).apply((Applicative<MineshaftFeatureConfig, ?>)instance, MineshaftFeatureConfig::new));
    public final float probability;
    public final MineshaftFeature.Type type;

    public MineshaftFeatureConfig(float probability, MineshaftFeature.Type type) {
        this.probability = probability;
        this.type = type;
    }
}

