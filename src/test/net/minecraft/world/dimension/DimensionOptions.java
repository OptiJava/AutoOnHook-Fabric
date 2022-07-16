/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

public final class DimensionOptions {
    public static final Codec<DimensionOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)DimensionType.REGISTRY_CODEC.fieldOf("type")).flatXmap(Codecs.createPresentValueChecker(), Codecs.createPresentValueChecker()).forGetter(DimensionOptions::getDimensionTypeSupplier), ((MapCodec)ChunkGenerator.CODEC.fieldOf("generator")).forGetter(DimensionOptions::getChunkGenerator)).apply((Applicative<DimensionOptions, ?>)instance, instance.stable(DimensionOptions::new)));
    public static final RegistryKey<DimensionOptions> OVERWORLD = RegistryKey.of(Registry.DIMENSION_KEY, new Identifier("overworld"));
    public static final RegistryKey<DimensionOptions> NETHER = RegistryKey.of(Registry.DIMENSION_KEY, new Identifier("the_nether"));
    public static final RegistryKey<DimensionOptions> END = RegistryKey.of(Registry.DIMENSION_KEY, new Identifier("the_end"));
    private static final Set<RegistryKey<DimensionOptions>> BASE_DIMENSIONS = Sets.newLinkedHashSet(ImmutableList.of(OVERWORLD, NETHER, END));
    private final Supplier<DimensionType> dimensionTypeSupplier;
    private final ChunkGenerator chunkGenerator;

    public DimensionOptions(Supplier<DimensionType> typeSupplier, ChunkGenerator chunkGenerator) {
        this.dimensionTypeSupplier = typeSupplier;
        this.chunkGenerator = chunkGenerator;
    }

    public Supplier<DimensionType> getDimensionTypeSupplier() {
        return this.dimensionTypeSupplier;
    }

    public DimensionType getDimensionType() {
        return this.dimensionTypeSupplier.get();
    }

    public ChunkGenerator getChunkGenerator() {
        return this.chunkGenerator;
    }

    public static SimpleRegistry<DimensionOptions> method_29569(SimpleRegistry<DimensionOptions> simpleRegistry) {
        Object dimensionOptions;
        SimpleRegistry<DimensionOptions> simpleRegistry2 = new SimpleRegistry<DimensionOptions>(Registry.DIMENSION_KEY, Lifecycle.experimental());
        for (RegistryKey<DimensionOptions> registryKey : BASE_DIMENSIONS) {
            dimensionOptions = simpleRegistry.get(registryKey);
            if (dimensionOptions == null) continue;
            simpleRegistry2.add(registryKey, dimensionOptions, simpleRegistry.getEntryLifecycle((DimensionOptions)dimensionOptions));
        }
        for (Map.Entry entry : simpleRegistry.getEntries()) {
            dimensionOptions = (RegistryKey)entry.getKey();
            if (BASE_DIMENSIONS.contains(dimensionOptions)) continue;
            simpleRegistry2.add((RegistryKey<DimensionOptions>)dimensionOptions, (DimensionOptions)entry.getValue(), simpleRegistry.getEntryLifecycle((DimensionOptions)entry.getValue()));
        }
        return simpleRegistry2;
    }

    public static boolean hasDefaultSettings(long seed, SimpleRegistry<DimensionOptions> options) {
        ArrayList<Map.Entry<RegistryKey<DimensionOptions>, DimensionOptions>> list = Lists.newArrayList(options.getEntries());
        if (list.size() != BASE_DIMENSIONS.size()) {
            return false;
        }
        Map.Entry entry = (Map.Entry)list.get(0);
        Map.Entry entry2 = (Map.Entry)list.get(1);
        Map.Entry entry3 = (Map.Entry)list.get(2);
        if (entry.getKey() != OVERWORLD || entry2.getKey() != NETHER || entry3.getKey() != END) {
            return false;
        }
        if (!((DimensionOptions)entry.getValue()).getDimensionType().equals(DimensionType.OVERWORLD) && ((DimensionOptions)entry.getValue()).getDimensionType() != DimensionType.OVERWORLD_CAVES) {
            return false;
        }
        if (!((DimensionOptions)entry2.getValue()).getDimensionType().equals(DimensionType.THE_NETHER)) {
            return false;
        }
        if (!((DimensionOptions)entry3.getValue()).getDimensionType().equals(DimensionType.THE_END)) {
            return false;
        }
        if (!(((DimensionOptions)entry2.getValue()).getChunkGenerator() instanceof NoiseChunkGenerator) || !(((DimensionOptions)entry3.getValue()).getChunkGenerator() instanceof NoiseChunkGenerator)) {
            return false;
        }
        NoiseChunkGenerator noiseChunkGenerator = (NoiseChunkGenerator)((DimensionOptions)entry2.getValue()).getChunkGenerator();
        NoiseChunkGenerator noiseChunkGenerator2 = (NoiseChunkGenerator)((DimensionOptions)entry3.getValue()).getChunkGenerator();
        if (!noiseChunkGenerator.matchesSettings(seed, ChunkGeneratorSettings.NETHER)) {
            return false;
        }
        if (!noiseChunkGenerator2.matchesSettings(seed, ChunkGeneratorSettings.END)) {
            return false;
        }
        if (!(noiseChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
            return false;
        }
        MultiNoiseBiomeSource multiNoiseBiomeSource = (MultiNoiseBiomeSource)noiseChunkGenerator.getBiomeSource();
        if (!multiNoiseBiomeSource.matchesInstance(seed)) {
            return false;
        }
        if (!(noiseChunkGenerator2.getBiomeSource() instanceof TheEndBiomeSource)) {
            return false;
        }
        TheEndBiomeSource theEndBiomeSource = (TheEndBiomeSource)noiseChunkGenerator2.getBiomeSource();
        return theEndBiomeSource.matches(seed);
    }
}

