/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome.source;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.CheckerboardBiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeSource
implements BiomeAccess.Storage {
    public static final Codec<BiomeSource> CODEC;
    protected final Map<StructureFeature<?>, Boolean> structureFeatures = Maps.newHashMap();
    protected final Set<BlockState> topMaterials = Sets.newHashSet();
    protected final List<Biome> biomes;

    protected BiomeSource(Stream<Supplier<Biome>> stream) {
        this(stream.map(Supplier::get).collect(ImmutableList.toImmutableList()));
    }

    protected BiomeSource(List<Biome> biomes) {
        this.biomes = biomes;
    }

    protected abstract Codec<? extends BiomeSource> getCodec();

    public abstract BiomeSource withSeed(long var1);

    public List<Biome> getBiomes() {
        return this.biomes;
    }

    public Set<Biome> getBiomesInArea(int x, int y, int z, int radius) {
        int i = BiomeCoords.fromBlock(x - radius);
        int j = BiomeCoords.fromBlock(y - radius);
        int k = BiomeCoords.fromBlock(z - radius);
        int l = BiomeCoords.fromBlock(x + radius);
        int m = BiomeCoords.fromBlock(y + radius);
        int n = BiomeCoords.fromBlock(z + radius);
        int o = l - i + 1;
        int p = m - j + 1;
        int q = n - k + 1;
        HashSet<Biome> set = Sets.newHashSet();
        for (int r = 0; r < q; ++r) {
            for (int s = 0; s < o; ++s) {
                for (int t = 0; t < p; ++t) {
                    int u = i + s;
                    int v = j + t;
                    int w = k + r;
                    set.add(this.getBiomeForNoiseGen(u, v, w));
                }
            }
        }
        return set;
    }

    @Nullable
    public BlockPos locateBiome(int x, int y, int z, int radius, Predicate<Biome> predicate, Random random) {
        return this.locateBiome(x, y, z, radius, 1, predicate, random, false);
    }

    @Nullable
    public BlockPos locateBiome(int x, int y, int z, int radius, int i, Predicate<Biome> predicate, Random random, boolean bl) {
        int o;
        int j = BiomeCoords.fromBlock(x);
        int k = BiomeCoords.fromBlock(z);
        int l = BiomeCoords.fromBlock(radius);
        int m = BiomeCoords.fromBlock(y);
        BlockPos blockPos = null;
        int n = 0;
        for (int p = o = bl ? 0 : l; p <= l; p += i) {
            for (int q = -p; q <= p; q += i) {
                boolean bl2 = Math.abs(q) == p;
                for (int r = -p; r <= p; r += i) {
                    int s;
                    int bl3;
                    if (bl) {
                        int n2 = bl3 = Math.abs(r) == p ? 1 : 0;
                        if (bl3 == 0 && !bl2) continue;
                    }
                    if (!predicate.test(this.getBiomeForNoiseGen(bl3 = j + r, m, s = k + q))) continue;
                    if (blockPos == null || random.nextInt(n + 1) == 0) {
                        blockPos = new BlockPos(BiomeCoords.toBlock(bl3), y, BiomeCoords.toBlock(s));
                        if (bl) {
                            return blockPos;
                        }
                    }
                    ++n;
                }
            }
        }
        return blockPos;
    }

    public boolean hasStructureFeature(StructureFeature<?> feature) {
        return this.structureFeatures.computeIfAbsent(feature, structureFeature -> this.biomes.stream().anyMatch(biome -> biome.getGenerationSettings().hasStructureFeature((StructureFeature<?>)structureFeature)));
    }

    public Set<BlockState> getTopMaterials() {
        if (this.topMaterials.isEmpty()) {
            for (Biome biome : this.biomes) {
                this.topMaterials.add(biome.getGenerationSettings().getSurfaceConfig().getTopMaterial());
            }
        }
        return this.topMaterials;
    }

    static {
        Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "vanilla_layered", VanillaLayeredBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
        CODEC = Registry.BIOME_SOURCE.dispatchStable(BiomeSource::getCodec, Function.identity());
    }
}

