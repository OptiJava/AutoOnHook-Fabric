/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class RandomFeatureEntry {
    public static final Codec<RandomFeatureEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredFeature.REGISTRY_CODEC.fieldOf("feature")).flatXmap(Codecs.createPresentValueChecker(), Codecs.createPresentValueChecker()).forGetter(randomFeatureEntry -> randomFeatureEntry.feature), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance")).forGetter(randomFeatureEntry -> Float.valueOf(randomFeatureEntry.chance))).apply((Applicative<RandomFeatureEntry, ?>)instance, RandomFeatureEntry::new));
    public final Supplier<ConfiguredFeature<?, ?>> feature;
    public final float chance;

    public RandomFeatureEntry(ConfiguredFeature<?, ?> feature, float chance) {
        this(() -> feature, chance);
    }

    private RandomFeatureEntry(Supplier<ConfiguredFeature<?, ?>> feature, float chance) {
        this.feature = feature;
        this.chance = chance;
    }

    public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos) {
        return this.feature.get().generate(world, chunkGenerator, random, pos);
    }
}

