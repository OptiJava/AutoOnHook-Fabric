/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;

public class GeodeCrackConfig {
    public static final Codec<GeodeCrackConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)GeodeFeatureConfig.RANGE.fieldOf("generate_crack_chance")).orElse(1.0).forGetter(geodeCrackConfig -> geodeCrackConfig.generateCrackChance), ((MapCodec)Codec.doubleRange(0.0, 5.0).fieldOf("base_crack_size")).orElse(2.0).forGetter(geodeCrackConfig -> geodeCrackConfig.baseCrackSize), ((MapCodec)Codec.intRange(0, 10).fieldOf("crack_point_offset")).orElse(2).forGetter(geodeCrackConfig -> geodeCrackConfig.crackPointOffset)).apply((Applicative<GeodeCrackConfig, ?>)instance, GeodeCrackConfig::new));
    public final double generateCrackChance;
    public final double baseCrackSize;
    public final int crackPointOffset;

    public GeodeCrackConfig(double generateCrackChance, double baseCrackSize, int crackPointOffset) {
        this.generateCrackChance = generateCrackChance;
        this.baseCrackSize = baseCrackSize;
        this.crackPointOffset = crackPointOffset;
    }
}

