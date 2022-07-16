/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.world.gen.feature.FeatureConfig;

/**
 * A feature config that specifies a starting pool and a size for {@linkplain
 * net.minecraft.structure.pool.StructurePoolBasedGenerator#generate}.
 */
public class StructurePoolFeatureConfig
implements FeatureConfig {
    public static final Codec<StructurePoolFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StructurePool.REGISTRY_CODEC.fieldOf("start_pool")).forGetter(StructurePoolFeatureConfig::getStartPool), ((MapCodec)Codec.intRange(0, 7).fieldOf("size")).forGetter(StructurePoolFeatureConfig::getSize)).apply((Applicative<StructurePoolFeatureConfig, ?>)instance, StructurePoolFeatureConfig::new));
    private final Supplier<StructurePool> startPool;
    private final int size;

    public StructurePoolFeatureConfig(Supplier<StructurePool> startPool, int size) {
        this.startPool = startPool;
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public Supplier<StructurePool> getStartPool() {
        return this.startPool;
    }
}

