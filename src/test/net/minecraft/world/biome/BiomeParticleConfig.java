/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public class BiomeParticleConfig {
    public static final Codec<BiomeParticleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("options")).forGetter(biomeParticleConfig -> biomeParticleConfig.particle), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(biomeParticleConfig -> Float.valueOf(biomeParticleConfig.probability))).apply((Applicative<BiomeParticleConfig, ?>)instance, BiomeParticleConfig::new));
    private final ParticleEffect particle;
    private final float probability;

    public BiomeParticleConfig(ParticleEffect particle, float probability) {
        this.particle = particle;
        this.probability = probability;
    }

    public ParticleEffect getParticle() {
        return this.particle;
    }

    public boolean shouldAddParticle(Random random) {
        return random.nextFloat() <= this.probability;
    }
}

