/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.world;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.CustomizeBuffetLevelScreen;
import net.minecraft.client.gui.screen.CustomizeFlatLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

@Environment(value=EnvType.CLIENT)
public abstract class GeneratorType {
    public static final GeneratorType DEFAULT = new GeneratorType("default"){

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new NoiseChunkGenerator(new VanillaLayeredBiomeSource(seed, false, false, biomeRegistry), seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.OVERWORLD));
        }
    };
    private static final GeneratorType FLAT = new GeneratorType("flat"){

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new FlatChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig(biomeRegistry));
        }
    };
    private static final GeneratorType LARGE_BIOMES = new GeneratorType("large_biomes"){

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new NoiseChunkGenerator(new VanillaLayeredBiomeSource(seed, false, true, biomeRegistry), seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.OVERWORLD));
        }
    };
    public static final GeneratorType AMPLIFIED = new GeneratorType("amplified"){

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new NoiseChunkGenerator(new VanillaLayeredBiomeSource(seed, false, false, biomeRegistry), seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.AMPLIFIED));
        }
    };
    private static final GeneratorType SINGLE_BIOME_SURFACE = new GeneratorType("single_biome_surface"){

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new NoiseChunkGenerator(new FixedBiomeSource(biomeRegistry.getOrThrow(BiomeKeys.PLAINS)), seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.OVERWORLD));
        }
    };
    private static final GeneratorType SINGLE_BIOME_CAVES = new GeneratorType("single_biome_caves"){

        @Override
        public GeneratorOptions createDefaultOptions(DynamicRegistryManager.Impl registryManager, long seed, boolean generateStructures, boolean bonusChest) {
            Registry<Biome> registry = registryManager.get(Registry.BIOME_KEY);
            Registry<DimensionType> registry2 = registryManager.get(Registry.DIMENSION_TYPE_KEY);
            Registry<ChunkGeneratorSettings> registry3 = registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY);
            return new GeneratorOptions(seed, generateStructures, bonusChest, GeneratorOptions.getRegistryWithReplacedOverworld(DimensionType.createDefaultDimensionOptions(registry2, registry, registry3, seed), () -> registry2.getOrThrow(DimensionType.OVERWORLD_CAVES_REGISTRY_KEY), this.getChunkGenerator(registry, registry3, seed)));
        }

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new NoiseChunkGenerator(new FixedBiomeSource(biomeRegistry.getOrThrow(BiomeKeys.PLAINS)), seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.CAVES));
        }
    };
    private static final GeneratorType SINGLE_BIOME_FLOATING_ISLANDS = new GeneratorType("single_biome_floating_islands"){

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new NoiseChunkGenerator(new FixedBiomeSource(biomeRegistry.getOrThrow(BiomeKeys.PLAINS)), seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.FLOATING_ISLANDS));
        }
    };
    private static final GeneratorType DEBUG_ALL_BLOCK_STATES = new GeneratorType("debug_all_block_states"){

        @Override
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            return new DebugChunkGenerator(biomeRegistry);
        }
    };
    protected static final List<GeneratorType> VALUES = Lists.newArrayList(DEFAULT, FLAT, LARGE_BIOMES, AMPLIFIED, SINGLE_BIOME_SURFACE, SINGLE_BIOME_CAVES, SINGLE_BIOME_FLOATING_ISLANDS, DEBUG_ALL_BLOCK_STATES);
    protected static final Map<Optional<GeneratorType>, ScreenProvider> SCREEN_PROVIDERS = ImmutableMap.of(Optional.of(FLAT), (screen, generatorOptions) -> {
        ChunkGenerator chunkGenerator = generatorOptions.getChunkGenerator();
        return new CustomizeFlatLevelScreen(screen, config -> createWorldScreen.moreOptionsDialog.setGeneratorOptions(new GeneratorOptions(generatorOptions.getSeed(), generatorOptions.shouldGenerateStructures(), generatorOptions.hasBonusChest(), GeneratorOptions.getRegistryWithReplacedOverworldGenerator(createWorldScreen.moreOptionsDialog.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY), generatorOptions.getDimensions(), new FlatChunkGenerator((FlatChunkGeneratorConfig)config)))), chunkGenerator instanceof FlatChunkGenerator ? ((FlatChunkGenerator)chunkGenerator).getConfig() : FlatChunkGeneratorConfig.getDefaultConfig(screen.moreOptionsDialog.getRegistryManager().get(Registry.BIOME_KEY)));
    }, Optional.of(SINGLE_BIOME_SURFACE), (screen, generatorOptions) -> new CustomizeBuffetLevelScreen(screen, screen.moreOptionsDialog.getRegistryManager(), biome -> createWorldScreen.moreOptionsDialog.setGeneratorOptions(GeneratorType.createFixedBiomeOptions(createWorldScreen.moreOptionsDialog.getRegistryManager(), generatorOptions, SINGLE_BIOME_SURFACE, biome)), GeneratorType.getFirstBiome(screen.moreOptionsDialog.getRegistryManager(), generatorOptions)), Optional.of(SINGLE_BIOME_CAVES), (screen, generatorOptions) -> new CustomizeBuffetLevelScreen(screen, screen.moreOptionsDialog.getRegistryManager(), biome -> createWorldScreen.moreOptionsDialog.setGeneratorOptions(GeneratorType.createFixedBiomeOptions(createWorldScreen.moreOptionsDialog.getRegistryManager(), generatorOptions, SINGLE_BIOME_CAVES, biome)), GeneratorType.getFirstBiome(screen.moreOptionsDialog.getRegistryManager(), generatorOptions)), Optional.of(SINGLE_BIOME_FLOATING_ISLANDS), (screen, generatorOptions) -> new CustomizeBuffetLevelScreen(screen, screen.moreOptionsDialog.getRegistryManager(), biome -> createWorldScreen.moreOptionsDialog.setGeneratorOptions(GeneratorType.createFixedBiomeOptions(createWorldScreen.moreOptionsDialog.getRegistryManager(), generatorOptions, SINGLE_BIOME_FLOATING_ISLANDS, biome)), GeneratorType.getFirstBiome(screen.moreOptionsDialog.getRegistryManager(), generatorOptions)));
    private final Text translationKey;

    GeneratorType(String string) {
        this.translationKey = new TranslatableText("generator." + string);
    }

    private static GeneratorOptions createFixedBiomeOptions(DynamicRegistryManager registryManager, GeneratorOptions generatorOptions, GeneratorType type, Biome biome) {
        FixedBiomeSource biomeSource = new FixedBiomeSource(biome);
        Registry<DimensionType> registry = registryManager.get(Registry.DIMENSION_TYPE_KEY);
        Registry<ChunkGeneratorSettings> registry2 = registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY);
        Supplier<ChunkGeneratorSettings> supplier = type == SINGLE_BIOME_CAVES ? () -> registry2.getOrThrow(ChunkGeneratorSettings.CAVES) : (type == SINGLE_BIOME_FLOATING_ISLANDS ? () -> registry2.getOrThrow(ChunkGeneratorSettings.FLOATING_ISLANDS) : () -> registry2.getOrThrow(ChunkGeneratorSettings.OVERWORLD));
        return new GeneratorOptions(generatorOptions.getSeed(), generatorOptions.shouldGenerateStructures(), generatorOptions.hasBonusChest(), GeneratorOptions.getRegistryWithReplacedOverworldGenerator(registry, generatorOptions.getDimensions(), new NoiseChunkGenerator(biomeSource, generatorOptions.getSeed(), supplier)));
    }

    private static Biome getFirstBiome(DynamicRegistryManager registryManager, GeneratorOptions options) {
        return options.getChunkGenerator().getBiomeSource().getBiomes().stream().findFirst().orElse(registryManager.get(Registry.BIOME_KEY).getOrThrow(BiomeKeys.PLAINS));
    }

    public static Optional<GeneratorType> fromGeneratorOptions(GeneratorOptions generatorOptions) {
        ChunkGenerator chunkGenerator = generatorOptions.getChunkGenerator();
        if (chunkGenerator instanceof FlatChunkGenerator) {
            return Optional.of(FLAT);
        }
        if (chunkGenerator instanceof DebugChunkGenerator) {
            return Optional.of(DEBUG_ALL_BLOCK_STATES);
        }
        return Optional.empty();
    }

    public Text getTranslationKey() {
        return this.translationKey;
    }

    public GeneratorOptions createDefaultOptions(DynamicRegistryManager.Impl registryManager, long seed, boolean generateStructures, boolean bonusChest) {
        Registry<Biome> registry = registryManager.get(Registry.BIOME_KEY);
        Registry<DimensionType> registry2 = registryManager.get(Registry.DIMENSION_TYPE_KEY);
        Registry<ChunkGeneratorSettings> registry3 = registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY);
        return new GeneratorOptions(seed, generateStructures, bonusChest, GeneratorOptions.getRegistryWithReplacedOverworldGenerator(registry2, DimensionType.createDefaultDimensionOptions(registry2, registry, registry3, seed), this.getChunkGenerator(registry, registry3, seed)));
    }

    protected abstract ChunkGenerator getChunkGenerator(Registry<Biome> var1, Registry<ChunkGeneratorSettings> var2, long var3);

    public static boolean isNotDebug(GeneratorType generatorType) {
        return generatorType != DEBUG_ALL_BLOCK_STATES;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface ScreenProvider {
        public Screen createEditScreen(CreateWorldScreen var1, GeneratorOptions var2);
    }
}
