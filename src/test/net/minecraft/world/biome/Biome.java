/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.dynamic.RegistryElementCodec;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.WorldGenRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<Biome> CODEC = RecordCodecBuilder.create(instance -> instance.group(Weather.CODEC.forGetter(biome -> biome.weather), ((MapCodec)Category.CODEC.fieldOf("category")).forGetter(biome -> biome.category), ((MapCodec)Codec.FLOAT.fieldOf("depth")).forGetter(biome -> Float.valueOf(biome.depth)), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(biome -> Float.valueOf(biome.scale)), ((MapCodec)BiomeEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.effects), GenerationSettings.CODEC.forGetter(biome -> biome.generationSettings), SpawnSettings.CODEC.forGetter(biome -> biome.spawnSettings)).apply((Applicative<Biome, ?>)instance, Biome::new));
    public static final Codec<Biome> field_26633 = RecordCodecBuilder.create(instance -> instance.group(Weather.CODEC.forGetter(biome -> biome.weather), ((MapCodec)Category.CODEC.fieldOf("category")).forGetter(biome -> biome.category), ((MapCodec)Codec.FLOAT.fieldOf("depth")).forGetter(biome -> Float.valueOf(biome.depth)), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(biome -> Float.valueOf(biome.scale)), ((MapCodec)BiomeEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.effects)).apply((Applicative<Biome, ?>)instance, (weather, category, float_, float2, biomeEffects) -> new Biome((Weather)weather, (Category)category, float_.floatValue(), float2.floatValue(), (BiomeEffects)biomeEffects, GenerationSettings.INSTANCE, SpawnSettings.INSTANCE)));
    public static final Codec<Supplier<Biome>> REGISTRY_CODEC = RegistryElementCodec.of(Registry.BIOME_KEY, CODEC);
    public static final Codec<List<Supplier<Biome>>> field_26750 = RegistryElementCodec.method_31194(Registry.BIOME_KEY, CODEC);
    private final Map<Integer, List<StructureFeature<?>>> structures = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy(structureFeature -> structureFeature.getGenerationStep().ordinal()));
    private static final OctaveSimplexNoiseSampler TEMPERATURE_NOISE = new OctaveSimplexNoiseSampler((WorldGenRandom)new ChunkRandom(1234L), ImmutableList.of(Integer.valueOf(0)));
    static final OctaveSimplexNoiseSampler FROZEN_OCEAN_NOISE = new OctaveSimplexNoiseSampler((WorldGenRandom)new ChunkRandom(3456L), ImmutableList.of(Integer.valueOf(-2), Integer.valueOf(-1), Integer.valueOf(0)));
    public static final OctaveSimplexNoiseSampler FOLIAGE_NOISE = new OctaveSimplexNoiseSampler((WorldGenRandom)new ChunkRandom(2345L), ImmutableList.of(Integer.valueOf(0)));
    private static final int field_30978 = 1024;
    private final Weather weather;
    private final GenerationSettings generationSettings;
    private final SpawnSettings spawnSettings;
    private final float depth;
    private final float scale;
    private final Category category;
    private final BiomeEffects effects;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25f){

            @Override
            protected void rehash(int i) {
            }
        };
        long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
        return long2FloatLinkedOpenHashMap;
    }));

    Biome(Weather weather, Category category, float depth, float scale, BiomeEffects effects, GenerationSettings generationSettings, SpawnSettings spawnSettings) {
        this.weather = weather;
        this.generationSettings = generationSettings;
        this.spawnSettings = spawnSettings;
        this.category = category;
        this.depth = depth;
        this.scale = scale;
        this.effects = effects;
    }

    public int getSkyColor() {
        return this.effects.getSkyColor();
    }

    public SpawnSettings getSpawnSettings() {
        return this.spawnSettings;
    }

    public Precipitation getPrecipitation() {
        return this.weather.precipitation;
    }

    public boolean hasHighHumidity() {
        return this.getDownfall() > 0.85f;
    }

    private float computeTemperature(BlockPos pos) {
        float f = this.weather.temperatureModifier.getModifiedTemperature(pos, this.getTemperature());
        if (pos.getY() > 64) {
            float g = (float)(TEMPERATURE_NOISE.sample((float)pos.getX() / 8.0f, (float)pos.getZ() / 8.0f, false) * 4.0);
            return f - (g + (float)pos.getY() - 64.0f) * 0.05f / 30.0f;
        }
        return f;
    }

    public final float getTemperature(BlockPos blockPos) {
        long l = blockPos.asLong();
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = this.temperatureCache.get();
        float f = long2FloatLinkedOpenHashMap.get(l);
        if (!Float.isNaN(f)) {
            return f;
        }
        float g = this.computeTemperature(blockPos);
        if (long2FloatLinkedOpenHashMap.size() == 1024) {
            long2FloatLinkedOpenHashMap.removeFirstFloat();
        }
        long2FloatLinkedOpenHashMap.put(l, g);
        return g;
    }

    public boolean canSetIce(WorldView world, BlockPos blockPos) {
        return this.canSetIce(world, blockPos, true);
    }

    public boolean canSetIce(WorldView world, BlockPos pos, boolean doWaterCheck) {
        if (this.getTemperature(pos) >= 0.15f) {
            return false;
        }
        if (pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, pos) < 10) {
            BlockState blockState = world.getBlockState(pos);
            FluidState fluidState = world.getFluidState(pos);
            if (fluidState.getFluid() == Fluids.WATER && blockState.getBlock() instanceof FluidBlock) {
                boolean bl;
                if (!doWaterCheck) {
                    return true;
                }
                boolean bl2 = bl = world.isWater(pos.west()) && world.isWater(pos.east()) && world.isWater(pos.north()) && world.isWater(pos.south());
                if (!bl) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCold(BlockPos pos) {
        return this.getTemperature(pos) < 0.15f;
    }

    public boolean canSetSnow(WorldView world, BlockPos blockPos) {
        BlockState blockState;
        if (!this.isCold(blockPos)) {
            return false;
        }
        return blockPos.getY() >= world.getBottomY() && blockPos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, blockPos) < 10 && (blockState = world.getBlockState(blockPos)).isAir() && Blocks.SNOW.getDefaultState().canPlaceAt(world, blockPos);
    }

    public GenerationSettings getGenerationSettings() {
        return this.generationSettings;
    }

    public void generateFeatureStep(StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, ChunkRegion region, long populationSeed, ChunkRandom random, BlockPos origin) {
        List<List<Supplier<ConfiguredFeature<?, ?>>>> list = this.generationSettings.getFeatures();
        Registry<ConfiguredFeature<?, ?>> registry = region.getRegistryManager().get(Registry.CONFIGURED_FEATURE_KEY);
        Registry<StructureFeature<?>> registry2 = region.getRegistryManager().get(Registry.STRUCTURE_FEATURE_KEY);
        int i = GenerationStep.Feature.values().length;
        for (int j = 0; j < i; ++j) {
            int k = 0;
            if (structureAccessor.shouldGenerateStructures()) {
                List list2 = this.structures.getOrDefault(j, Collections.emptyList());
                for (Object structureFeature : list2) {
                    random.setDecoratorSeed(populationSeed, k, j);
                    int l = ChunkSectionPos.getSectionCoord(origin.getX());
                    int m = ChunkSectionPos.getSectionCoord(origin.getZ());
                    int n = ChunkSectionPos.getBlockCoord(l);
                    int o = ChunkSectionPos.getBlockCoord(m);
                    Supplier<String> supplier = () -> Biome.method_28406(registry2, (StructureFeature)structureFeature);
                    try {
                        int p = region.getBottomY() + 1;
                        int q = region.getTopY() - 1;
                        region.method_36972(supplier);
                        structureAccessor.getStructuresWithChildren(ChunkSectionPos.from(origin), (StructureFeature<?>)structureFeature).forEach(structureStart -> structureStart.generateStructure(region, structureAccessor, chunkGenerator, random, new BlockBox(n, p, o, n + 15, q, o + 15), new ChunkPos(l, m)));
                    }
                    catch (Exception p) {
                        CrashReport q = CrashReport.create(p, "Feature placement");
                        q.addElement("Feature").add("Description", supplier::get);
                        throw new CrashException(q);
                    }
                    ++k;
                }
            }
            if (list.size() <= j) continue;
            for (Supplier<ConfiguredFeature<?, ?>> supplier2 : list.get(j)) {
                Object structureFeature;
                structureFeature = supplier2.get();
                Supplier<String> l = () -> Biome.method_23348(registry, (ConfiguredFeature)structureFeature);
                random.setDecoratorSeed(populationSeed, k, j);
                try {
                    region.method_36972(l);
                    ((ConfiguredFeature)structureFeature).generate(region, chunkGenerator, random, origin);
                }
                catch (Exception m) {
                    CrashReport n = CrashReport.create(m, "Feature placement");
                    n.addElement("Feature").add("Description", l::get);
                    throw new CrashException(n);
                }
                ++k;
            }
        }
        region.method_36972(null);
    }

    public int getFogColor() {
        return this.effects.getFogColor();
    }

    public int getGrassColorAt(double x, double z) {
        int i = this.effects.getGrassColor().orElseGet(this::getDefaultGrassColor);
        return this.effects.getGrassColorModifier().getModifiedGrassColor(x, z, i);
    }

    private int getDefaultGrassColor() {
        double d = MathHelper.clamp(this.weather.temperature, 0.0f, 1.0f);
        double e = MathHelper.clamp(this.weather.downfall, 0.0f, 1.0f);
        return GrassColors.getColor(d, e);
    }

    public int getFoliageColor() {
        return this.effects.getFoliageColor().orElseGet(this::getDefaultFoliageColor);
    }

    private int getDefaultFoliageColor() {
        double d = MathHelper.clamp(this.weather.temperature, 0.0f, 1.0f);
        double e = MathHelper.clamp(this.weather.downfall, 0.0f, 1.0f);
        return FoliageColors.getColor(d, e);
    }

    public void buildSurface(Random random, Chunk chunk, int x, int z, int worldHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, int i, long l) {
        ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder = this.generationSettings.getSurfaceBuilder().get();
        configuredSurfaceBuilder.initSeed(l);
        configuredSurfaceBuilder.generate(random, chunk, this, x, z, worldHeight, noise, defaultBlock, defaultFluid, seaLevel, i, l);
    }

    public final float getDepth() {
        return this.depth;
    }

    public final float getDownfall() {
        return this.weather.downfall;
    }

    public final float getScale() {
        return this.scale;
    }

    public final float getTemperature() {
        return this.weather.temperature;
    }

    public BiomeEffects getEffects() {
        return this.effects;
    }

    public final int getWaterColor() {
        return this.effects.getWaterColor();
    }

    public final int getWaterFogColor() {
        return this.effects.getWaterFogColor();
    }

    public Optional<BiomeParticleConfig> getParticleConfig() {
        return this.effects.getParticleConfig();
    }

    public Optional<SoundEvent> getLoopSound() {
        return this.effects.getLoopSound();
    }

    public Optional<BiomeMoodSound> getMoodSound() {
        return this.effects.getMoodSound();
    }

    public Optional<BiomeAdditionsSound> getAdditionsSound() {
        return this.effects.getAdditionsSound();
    }

    public Optional<MusicSound> getMusic() {
        return this.effects.getMusic();
    }

    public final Category getCategory() {
        return this.category;
    }

    public String toString() {
        Identifier identifier = BuiltinRegistries.BIOME.getId(this);
        return identifier == null ? super.toString() : identifier.toString();
    }

    private static /* synthetic */ String method_23348(Registry registry, ConfiguredFeature configuredFeature) {
        return registry.getKey(configuredFeature).map(Object::toString).orElseGet(configuredFeature::toString);
    }

    private static /* synthetic */ String method_28406(Registry registry, StructureFeature structureFeature) {
        return registry.getKey(structureFeature).map(Object::toString).orElseGet(structureFeature::toString);
    }

    static class Weather {
        public static final MapCodec<Weather> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Precipitation.CODEC.fieldOf("precipitation")).forGetter(weather -> weather.precipitation), ((MapCodec)Codec.FLOAT.fieldOf("temperature")).forGetter(weather -> Float.valueOf(weather.temperature)), TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", TemperatureModifier.NONE).forGetter(weather -> weather.temperatureModifier), ((MapCodec)Codec.FLOAT.fieldOf("downfall")).forGetter(weather -> Float.valueOf(weather.downfall))).apply((Applicative<Weather, ?>)instance, Weather::new));
        final Precipitation precipitation;
        final float temperature;
        final TemperatureModifier temperatureModifier;
        final float downfall;

        Weather(Precipitation precipitation, float f, TemperatureModifier temperatureModifier, float g) {
            this.precipitation = precipitation;
            this.temperature = f;
            this.temperatureModifier = temperatureModifier;
            this.downfall = g;
        }
    }

    public static enum Category implements StringIdentifiable
    {
        NONE("none"),
        TAIGA("taiga"),
        EXTREME_HILLS("extreme_hills"),
        JUNGLE("jungle"),
        MESA("mesa"),
        PLAINS("plains"),
        SAVANNA("savanna"),
        ICY("icy"),
        THEEND("the_end"),
        BEACH("beach"),
        FOREST("forest"),
        OCEAN("ocean"),
        DESERT("desert"),
        RIVER("river"),
        SWAMP("swamp"),
        MUSHROOM("mushroom"),
        NETHER("nether"),
        UNDERGROUND("underground");

        public static final Codec<Category> CODEC;
        private static final Map<String, Category> BY_NAME;
        private final String name;

        private Category(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static Category byName(String name) {
            return BY_NAME.get(name);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Category::values, Category::byName);
            BY_NAME = Arrays.stream(Category.values()).collect(Collectors.toMap(Category::getName, category -> category));
        }
    }

    public static enum Precipitation implements StringIdentifiable
    {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Precipitation> CODEC;
        private static final Map<String, Precipitation> BY_NAME;
        private final String name;

        private Precipitation(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static Precipitation byName(String name) {
            return BY_NAME.get(name);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Precipitation::values, Precipitation::byName);
            BY_NAME = Arrays.stream(Precipitation.values()).collect(Collectors.toMap(Precipitation::getName, precipitation -> precipitation));
        }
    }

    public static enum TemperatureModifier implements StringIdentifiable
    {
        NONE("none"){

            @Override
            public float getModifiedTemperature(BlockPos pos, float temperature) {
                return temperature;
            }
        }
        ,
        FROZEN("frozen"){

            @Override
            public float getModifiedTemperature(BlockPos pos, float temperature) {
                double g;
                double e;
                double d = FROZEN_OCEAN_NOISE.sample((double)pos.getX() * 0.05, (double)pos.getZ() * 0.05, false) * 7.0;
                double f = d + (e = FOLIAGE_NOISE.sample((double)pos.getX() * 0.2, (double)pos.getZ() * 0.2, false));
                if (f < 0.3 && (g = FOLIAGE_NOISE.sample((double)pos.getX() * 0.09, (double)pos.getZ() * 0.09, false)) < 0.8) {
                    return 0.2f;
                }
                return temperature;
            }
        };

        private final String name;
        public static final Codec<TemperatureModifier> CODEC;
        private static final Map<String, TemperatureModifier> BY_NAME;

        public abstract float getModifiedTemperature(BlockPos var1, float var2);

        TemperatureModifier(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public static TemperatureModifier byName(String name) {
            return BY_NAME.get(name);
        }

        static {
            CODEC = StringIdentifiable.createCodec(TemperatureModifier::values, TemperatureModifier::byName);
            BY_NAME = Arrays.stream(TemperatureModifier.values()).collect(Collectors.toMap(TemperatureModifier::getName, temperatureModifier -> temperatureModifier));
        }
    }

    public static class MixedNoisePoint {
        public static final Codec<MixedNoisePoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("temperature")).forGetter(mixedNoisePoint -> Float.valueOf(mixedNoisePoint.temperature)), ((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("humidity")).forGetter(mixedNoisePoint -> Float.valueOf(mixedNoisePoint.humidity)), ((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("altitude")).forGetter(mixedNoisePoint -> Float.valueOf(mixedNoisePoint.altitude)), ((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("weirdness")).forGetter(mixedNoisePoint -> Float.valueOf(mixedNoisePoint.weirdness)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("offset")).forGetter(mixedNoisePoint -> Float.valueOf(mixedNoisePoint.weight))).apply((Applicative<MixedNoisePoint, ?>)instance, MixedNoisePoint::new));
        private final float temperature;
        private final float humidity;
        private final float altitude;
        private final float weirdness;
        private final float weight;

        public MixedNoisePoint(float temperature, float humidity, float altitude, float weirdness, float weight) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.altitude = altitude;
            this.weirdness = weirdness;
            this.weight = weight;
        }

        public String toString() {
            return "temp: " + this.temperature + ", hum: " + this.humidity + ", alt: " + this.altitude + ", weird: " + this.weirdness + ", offset: " + this.weight;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            MixedNoisePoint mixedNoisePoint = (MixedNoisePoint)o;
            if (Float.compare(mixedNoisePoint.temperature, this.temperature) != 0) {
                return false;
            }
            if (Float.compare(mixedNoisePoint.humidity, this.humidity) != 0) {
                return false;
            }
            if (Float.compare(mixedNoisePoint.altitude, this.altitude) != 0) {
                return false;
            }
            return Float.compare(mixedNoisePoint.weirdness, this.weirdness) == 0;
        }

        public int hashCode() {
            int i = this.temperature != 0.0f ? Float.floatToIntBits(this.temperature) : 0;
            i = 31 * i + (this.humidity != 0.0f ? Float.floatToIntBits(this.humidity) : 0);
            i = 31 * i + (this.altitude != 0.0f ? Float.floatToIntBits(this.altitude) : 0);
            i = 31 * i + (this.weirdness != 0.0f ? Float.floatToIntBits(this.weirdness) : 0);
            return i;
        }

        public float calculateDistanceTo(MixedNoisePoint other) {
            return (this.temperature - other.temperature) * (this.temperature - other.temperature) + (this.humidity - other.humidity) * (this.humidity - other.humidity) + (this.altitude - other.altitude) * (this.altitude - other.altitude) + (this.weirdness - other.weirdness) * (this.weirdness - other.weirdness) + (this.weight - other.weight) * (this.weight - other.weight);
        }
    }

    public static class Builder {
        @Nullable
        private Precipitation precipitation;
        @Nullable
        private Category category;
        @Nullable
        private Float depth;
        @Nullable
        private Float scale;
        @Nullable
        private Float temperature;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;
        @Nullable
        private Float downfall;
        @Nullable
        private BiomeEffects specialEffects;
        @Nullable
        private SpawnSettings spawnSettings;
        @Nullable
        private GenerationSettings generationSettings;

        public Builder precipitation(Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder depth(float depth) {
            this.depth = Float.valueOf(depth);
            return this;
        }

        public Builder scale(float scale) {
            this.scale = Float.valueOf(scale);
            return this;
        }

        public Builder temperature(float temperature) {
            this.temperature = Float.valueOf(temperature);
            return this;
        }

        public Builder downfall(float downfall) {
            this.downfall = Float.valueOf(downfall);
            return this;
        }

        public Builder effects(BiomeEffects effects) {
            this.specialEffects = effects;
            return this;
        }

        public Builder spawnSettings(SpawnSettings spawnSettings) {
            this.spawnSettings = spawnSettings;
            return this;
        }

        public Builder generationSettings(GenerationSettings generationSettings) {
            this.generationSettings = generationSettings;
            return this;
        }

        public Builder temperatureModifier(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Biome build() {
            if (this.precipitation == null || this.category == null || this.depth == null || this.scale == null || this.temperature == null || this.downfall == null || this.specialEffects == null || this.spawnSettings == null || this.generationSettings == null) {
                throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
            }
            return new Biome(new Weather(this.precipitation, this.temperature.floatValue(), this.temperatureModifier, this.downfall.floatValue()), this.category, this.depth.floatValue(), this.scale.floatValue(), this.specialEffects, this.generationSettings, this.spawnSettings);
        }

        public String toString() {
            return "BiomeBuilder{\nprecipitation=" + this.precipitation + ",\nbiomeCategory=" + this.category + ",\ndepth=" + this.depth + ",\nscale=" + this.scale + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nmobSpawnSettings=" + this.spawnSettings + ",\ngenerationSettings=" + this.generationSettings + ",\n}";
        }
    }
}

