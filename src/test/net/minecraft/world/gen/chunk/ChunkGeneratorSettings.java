/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.chunk;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryElementCodec;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.NoiseSamplingConfig;
import net.minecraft.world.gen.chunk.SlideConfig;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public final class ChunkGeneratorSettings {
    public static final Codec<ChunkGeneratorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StructuresConfig.CODEC.fieldOf("structures")).forGetter(ChunkGeneratorSettings::getStructuresConfig), ((MapCodec)GenerationShapeConfig.CODEC.fieldOf("noise")).forGetter(ChunkGeneratorSettings::getGenerationShapeConfig), ((MapCodec)BlockState.CODEC.fieldOf("default_block")).forGetter(ChunkGeneratorSettings::getDefaultBlock), ((MapCodec)BlockState.CODEC.fieldOf("default_fluid")).forGetter(ChunkGeneratorSettings::getDefaultFluid), ((MapCodec)Codec.INT.fieldOf("bedrock_roof_position")).forGetter(ChunkGeneratorSettings::getBedrockCeilingY), ((MapCodec)Codec.INT.fieldOf("bedrock_floor_position")).forGetter(ChunkGeneratorSettings::getBedrockFloorY), ((MapCodec)Codec.INT.fieldOf("sea_level")).forGetter(ChunkGeneratorSettings::getSeaLevel), ((MapCodec)Codec.INT.fieldOf("min_surface_level")).forGetter(ChunkGeneratorSettings::getMinSurfaceLevel), ((MapCodec)Codec.BOOL.fieldOf("disable_mob_generation")).forGetter(ChunkGeneratorSettings::isMobGenerationDisabled), ((MapCodec)Codec.BOOL.fieldOf("aquifers_enabled")).forGetter(ChunkGeneratorSettings::hasAquifers), ((MapCodec)Codec.BOOL.fieldOf("noise_caves_enabled")).forGetter(ChunkGeneratorSettings::hasNoiseCaves), ((MapCodec)Codec.BOOL.fieldOf("deepslate_enabled")).forGetter(ChunkGeneratorSettings::hasDeepslate), ((MapCodec)Codec.BOOL.fieldOf("ore_veins_enabled")).forGetter(ChunkGeneratorSettings::hasOreVeins), ((MapCodec)Codec.BOOL.fieldOf("noodle_caves_enabled")).forGetter(ChunkGeneratorSettings::hasOreVeins)).apply((Applicative<ChunkGeneratorSettings, ?>)instance, ChunkGeneratorSettings::new));
    public static final Codec<Supplier<ChunkGeneratorSettings>> REGISTRY_CODEC = RegistryElementCodec.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, CODEC);
    private final StructuresConfig structuresConfig;
    private final GenerationShapeConfig generationShapeConfig;
    private final BlockState defaultBlock;
    private final BlockState defaultFluid;
    private final int bedrockCeilingY;
    private final int bedrockFloorY;
    private final int seaLevel;
    private final int minSurfaceLevel;
    private final boolean mobGenerationDisabled;
    private final boolean aquifers;
    private final boolean noiseCaves;
    private final boolean deepslate;
    private final boolean oreVeins;
    private final boolean noodleCaves;
    public static final RegistryKey<ChunkGeneratorSettings> OVERWORLD = RegistryKey.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, new Identifier("overworld"));
    public static final RegistryKey<ChunkGeneratorSettings> AMPLIFIED = RegistryKey.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, new Identifier("amplified"));
    public static final RegistryKey<ChunkGeneratorSettings> NETHER = RegistryKey.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, new Identifier("nether"));
    public static final RegistryKey<ChunkGeneratorSettings> END = RegistryKey.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, new Identifier("end"));
    public static final RegistryKey<ChunkGeneratorSettings> CAVES = RegistryKey.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, new Identifier("caves"));
    public static final RegistryKey<ChunkGeneratorSettings> FLOATING_ISLANDS = RegistryKey.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, new Identifier("floating_islands"));
    private static final ChunkGeneratorSettings INSTANCE = ChunkGeneratorSettings.register(OVERWORLD, ChunkGeneratorSettings.createSurfaceSettings(new StructuresConfig(true), false));

    private ChunkGeneratorSettings(StructuresConfig structuresConfig, GenerationShapeConfig generationShapeConfig, BlockState defaultBlock, BlockState defaultFluid, int bedrockCeilingY, int bedrockFloorY, int seaLevel, int minSurfaceLevel, boolean mobGenerationDisabled, boolean aquifers, boolean noiseCaves, boolean deepslate, boolean oreVeins, boolean noodleCaves) {
        this.structuresConfig = structuresConfig;
        this.generationShapeConfig = generationShapeConfig;
        this.defaultBlock = defaultBlock;
        this.defaultFluid = defaultFluid;
        this.bedrockCeilingY = bedrockCeilingY;
        this.bedrockFloorY = bedrockFloorY;
        this.seaLevel = seaLevel;
        this.minSurfaceLevel = minSurfaceLevel;
        this.mobGenerationDisabled = mobGenerationDisabled;
        this.aquifers = aquifers;
        this.noiseCaves = noiseCaves;
        this.deepslate = deepslate;
        this.oreVeins = oreVeins;
        this.noodleCaves = noodleCaves;
    }

    public StructuresConfig getStructuresConfig() {
        return this.structuresConfig;
    }

    public GenerationShapeConfig getGenerationShapeConfig() {
        return this.generationShapeConfig;
    }

    public BlockState getDefaultBlock() {
        return this.defaultBlock;
    }

    public BlockState getDefaultFluid() {
        return this.defaultFluid;
    }

    /**
     * Returns the Y level of the bedrock ceiling.
     * 
     * <p>If a number less than 1 is returned, the ceiling will not be generated.
     */
    public int getBedrockCeilingY() {
        return this.bedrockCeilingY;
    }

    /**
     * Returns the Y level of the bedrock floor.
     * 
     * <p>If a number greater than 255 is returned, the floor will not be generated.
     */
    public int getBedrockFloorY() {
        return this.bedrockFloorY;
    }

    public int getSeaLevel() {
        return this.seaLevel;
    }

    public int getMinSurfaceLevel() {
        return this.minSurfaceLevel;
    }

    /**
     * Whether entities will be generated during chunk population.
     * 
     * <p>It does not control whether spawns will occur during gameplay.
     */
    @Deprecated
    protected boolean isMobGenerationDisabled() {
        return this.mobGenerationDisabled;
    }

    protected boolean hasAquifers() {
        return this.aquifers;
    }

    protected boolean hasNoiseCaves() {
        return this.noiseCaves;
    }

    protected boolean hasDeepslate() {
        return this.deepslate;
    }

    protected boolean hasOreVeins() {
        return this.oreVeins;
    }

    protected boolean hasNoodleCaves() {
        return this.noodleCaves;
    }

    public boolean equals(RegistryKey<ChunkGeneratorSettings> registryKey) {
        return Objects.equals(this, BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.get(registryKey));
    }

    private static ChunkGeneratorSettings register(RegistryKey<ChunkGeneratorSettings> registryKey, ChunkGeneratorSettings settings) {
        BuiltinRegistries.add(BuiltinRegistries.CHUNK_GENERATOR_SETTINGS, registryKey.getValue(), settings);
        return settings;
    }

    public static ChunkGeneratorSettings getInstance() {
        return INSTANCE;
    }

    private static ChunkGeneratorSettings createIslandSettings(StructuresConfig structuresConfig, BlockState defaultBlock, BlockState defaultFluid, boolean bl, boolean bl2) {
        return new ChunkGeneratorSettings(structuresConfig, GenerationShapeConfig.create(0, 128, new NoiseSamplingConfig(2.0, 1.0, 80.0, 160.0), new SlideConfig(-3000, 64, -46), new SlideConfig(-30, 7, 1), 2, 1, 0.0, 0.0, true, false, bl2, false), defaultBlock, defaultFluid, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, bl, false, false, false, false, false);
    }

    private static ChunkGeneratorSettings createUndergroundSettings(StructuresConfig structuresConfig, BlockState defaultBlock, BlockState defaultFluid) {
        HashMap<StructureFeature<?>, StructureConfig> map = Maps.newHashMap(StructuresConfig.DEFAULT_STRUCTURES);
        map.put(StructureFeature.RUINED_PORTAL, new StructureConfig(25, 10, 34222645));
        return new ChunkGeneratorSettings(new StructuresConfig(Optional.ofNullable(structuresConfig.getStronghold()), map), GenerationShapeConfig.create(0, 128, new NoiseSamplingConfig(1.0, 3.0, 80.0, 60.0), new SlideConfig(120, 3, 0), new SlideConfig(320, 4, -1), 1, 2, 0.0, 0.019921875, false, false, false, false), defaultBlock, defaultFluid, 0, 0, 32, 0, false, false, false, false, false, false);
    }

    private static ChunkGeneratorSettings createSurfaceSettings(StructuresConfig structuresConfig, boolean amplified) {
        double d = 0.9999999814507745;
        return new ChunkGeneratorSettings(structuresConfig, GenerationShapeConfig.create(0, 256, new NoiseSamplingConfig(0.9999999814507745, 0.9999999814507745, 80.0, 160.0), new SlideConfig(-10, 3, 0), new SlideConfig(15, 3, 0), 1, 2, 1.0, -0.46875, true, true, false, amplified), Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), Integer.MIN_VALUE, 0, 63, 0, false, false, false, false, false, false);
    }

    static {
        ChunkGeneratorSettings.register(AMPLIFIED, ChunkGeneratorSettings.createSurfaceSettings(new StructuresConfig(true), true));
        ChunkGeneratorSettings.register(NETHER, ChunkGeneratorSettings.createUndergroundSettings(new StructuresConfig(false), Blocks.NETHERRACK.getDefaultState(), Blocks.LAVA.getDefaultState()));
        ChunkGeneratorSettings.register(END, ChunkGeneratorSettings.createIslandSettings(new StructuresConfig(false), Blocks.END_STONE.getDefaultState(), Blocks.AIR.getDefaultState(), true, true));
        ChunkGeneratorSettings.register(CAVES, ChunkGeneratorSettings.createUndergroundSettings(new StructuresConfig(true), Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState()));
        ChunkGeneratorSettings.register(FLOATING_ISLANDS, ChunkGeneratorSettings.createIslandSettings(new StructuresConfig(true), Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), false, false));
    }
}

