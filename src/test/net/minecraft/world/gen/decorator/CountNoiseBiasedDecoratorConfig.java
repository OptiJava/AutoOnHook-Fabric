/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.decorator.DecoratorConfig;

public class CountNoiseBiasedDecoratorConfig
implements DecoratorConfig {
    public static final Codec<CountNoiseBiasedDecoratorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("noise_to_count_ratio")).forGetter(countNoiseBiasedDecoratorConfig -> countNoiseBiasedDecoratorConfig.noiseToCountRatio), ((MapCodec)Codec.DOUBLE.fieldOf("noise_factor")).forGetter(countNoiseBiasedDecoratorConfig -> countNoiseBiasedDecoratorConfig.noiseFactor), ((MapCodec)Codec.DOUBLE.fieldOf("noise_offset")).orElse(0.0).forGetter(countNoiseBiasedDecoratorConfig -> countNoiseBiasedDecoratorConfig.noiseOffset)).apply((Applicative<CountNoiseBiasedDecoratorConfig, ?>)instance, CountNoiseBiasedDecoratorConfig::new));
    public final int noiseToCountRatio;
    public final double noiseFactor;
    public final double noiseOffset;

    public CountNoiseBiasedDecoratorConfig(int noiseToCountRatio, double noiseFactor, double noiseOffset) {
        this.noiseToCountRatio = noiseToCountRatio;
        this.noiseFactor = noiseFactor;
        this.noiseOffset = noiseOffset;
    }
}

