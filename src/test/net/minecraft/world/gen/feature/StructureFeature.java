/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.BastionRemnantFeature;
import net.minecraft.world.gen.feature.BuriedTreasureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.DesertPyramidFeature;
import net.minecraft.world.gen.feature.EndCityFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.IglooFeature;
import net.minecraft.world.gen.feature.JungleTempleFeature;
import net.minecraft.world.gen.feature.MineshaftFeature;
import net.minecraft.world.gen.feature.MineshaftFeatureConfig;
import net.minecraft.world.gen.feature.NetherFortressFeature;
import net.minecraft.world.gen.feature.NetherFossilFeature;
import net.minecraft.world.gen.feature.OceanMonumentFeature;
import net.minecraft.world.gen.feature.OceanRuinFeature;
import net.minecraft.world.gen.feature.OceanRuinFeatureConfig;
import net.minecraft.world.gen.feature.PillagerOutpostFeature;
import net.minecraft.world.gen.feature.RuinedPortalFeature;
import net.minecraft.world.gen.feature.RuinedPortalFeatureConfig;
import net.minecraft.world.gen.feature.ShipwreckFeature;
import net.minecraft.world.gen.feature.ShipwreckFeatureConfig;
import net.minecraft.world.gen.feature.StrongholdFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.minecraft.world.gen.feature.SwampHutFeature;
import net.minecraft.world.gen.feature.VillageFeature;
import net.minecraft.world.gen.feature.WoodlandMansionFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class StructureFeature<C extends FeatureConfig> {
    public static final BiMap<String, StructureFeature<?>> STRUCTURES = HashBiMap.create();
    private static final Map<StructureFeature<?>, GenerationStep.Feature> STRUCTURE_TO_GENERATION_STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<StructurePoolFeatureConfig> PILLAGER_OUTPOST = StructureFeature.register("Pillager_Outpost", new PillagerOutpostFeature(StructurePoolFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<MineshaftFeatureConfig> MINESHAFT = StructureFeature.register("Mineshaft", new MineshaftFeature(MineshaftFeatureConfig.CODEC), GenerationStep.Feature.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<DefaultFeatureConfig> MANSION = StructureFeature.register("Mansion", new WoodlandMansionFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<DefaultFeatureConfig> JUNGLE_PYRAMID = StructureFeature.register("Jungle_Pyramid", new JungleTempleFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<DefaultFeatureConfig> DESERT_PYRAMID = StructureFeature.register("Desert_Pyramid", new DesertPyramidFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<DefaultFeatureConfig> IGLOO = StructureFeature.register("Igloo", new IglooFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<RuinedPortalFeatureConfig> RUINED_PORTAL = StructureFeature.register("Ruined_Portal", new RuinedPortalFeature(RuinedPortalFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<ShipwreckFeatureConfig> SHIPWRECK = StructureFeature.register("Shipwreck", new ShipwreckFeature(ShipwreckFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final SwampHutFeature SWAMP_HUT = StructureFeature.register("Swamp_Hut", new SwampHutFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<DefaultFeatureConfig> STRONGHOLD = StructureFeature.register("Stronghold", new StrongholdFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.STRONGHOLDS);
    public static final StructureFeature<DefaultFeatureConfig> MONUMENT = StructureFeature.register("Monument", new OceanMonumentFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<OceanRuinFeatureConfig> OCEAN_RUIN = StructureFeature.register("Ocean_Ruin", new OceanRuinFeature(OceanRuinFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<DefaultFeatureConfig> FORTRESS = StructureFeature.register("Fortress", new NetherFortressFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.UNDERGROUND_DECORATION);
    public static final StructureFeature<DefaultFeatureConfig> END_CITY = StructureFeature.register("EndCity", new EndCityFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<ProbabilityConfig> BURIED_TREASURE = StructureFeature.register("Buried_Treasure", new BuriedTreasureFeature(ProbabilityConfig.CODEC), GenerationStep.Feature.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<StructurePoolFeatureConfig> VILLAGE = StructureFeature.register("Village", new VillageFeature(StructurePoolFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final StructureFeature<RangeDecoratorConfig> NETHER_FOSSIL = StructureFeature.register("Nether_Fossil", new NetherFossilFeature(RangeDecoratorConfig.CODEC), GenerationStep.Feature.UNDERGROUND_DECORATION);
    public static final StructureFeature<StructurePoolFeatureConfig> BASTION_REMNANT = StructureFeature.register("Bastion_Remnant", new BastionRemnantFeature(StructurePoolFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
    public static final List<StructureFeature<?>> LAND_MODIFYING_STRUCTURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
    private static final Identifier JIGSAW_ID = new Identifier("jigsaw");
    private static final Map<Identifier, Identifier> JIGSAW_STRUCTURE_PIECES = ImmutableMap.builder().put(new Identifier("nvi"), JIGSAW_ID).put(new Identifier("pcp"), JIGSAW_ID).put(new Identifier("bastionremnant"), JIGSAW_ID).put(new Identifier("runtime"), JIGSAW_ID).build();
    public static final int field_31518 = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> codec;

    private static <F extends StructureFeature<?>> F register(String name, F structureFeature, GenerationStep.Feature step) {
        STRUCTURES.put(name.toLowerCase(Locale.ROOT), structureFeature);
        STRUCTURE_TO_GENERATION_STEP.put(structureFeature, step);
        return (F)Registry.register(Registry.STRUCTURE_FEATURE, name.toLowerCase(Locale.ROOT), structureFeature);
    }

    public StructureFeature(Codec<C> codec) {
        this.codec = ((MapCodec)codec.fieldOf("config")).xmap(featureConfig -> new ConfiguredStructureFeature<FeatureConfig, StructureFeature>(this, (FeatureConfig)featureConfig), configuredStructureFeature -> configuredStructureFeature.config).codec();
    }

    /**
     * Gets the step during which this structure will participate in world generation.
     * Structures will generate before other features in the same generation step.
     */
    public GenerationStep.Feature getGenerationStep() {
        return STRUCTURE_TO_GENERATION_STEP.get(this);
    }

    public static void init() {
    }

    @Nullable
    public static StructureStart<?> readStructureStart(ServerWorld world, NbtCompound nbt, long worldSeed) {
        String string = nbt.getString("id");
        if ("INVALID".equals(string)) {
            return StructureStart.DEFAULT;
        }
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new Identifier(string.toLowerCase(Locale.ROOT)));
        if (structureFeature == null) {
            LOGGER.error("Unknown feature id: {}", (Object)string);
            return null;
        }
        ChunkPos chunkPos = new ChunkPos(nbt.getInt("ChunkX"), nbt.getInt("ChunkZ"));
        int i = nbt.getInt("references");
        NbtList nbtList = nbt.getList("Children", 10);
        try {
            StructureStart<?> structureStart = structureFeature.createStart(chunkPos, i, worldSeed);
            for (int j = 0; j < nbtList.size(); ++j) {
                NbtCompound nbtCompound = nbtList.getCompound(j);
                String string2 = nbtCompound.getString("id").toLowerCase(Locale.ROOT);
                Identifier identifier = new Identifier(string2);
                Identifier identifier2 = JIGSAW_STRUCTURE_PIECES.getOrDefault(identifier, identifier);
                StructurePieceType structurePieceType = Registry.STRUCTURE_PIECE.get(identifier2);
                if (structurePieceType == null) {
                    LOGGER.error("Unknown structure piece id: {}", (Object)identifier2);
                    continue;
                }
                try {
                    StructurePiece structurePiece = structurePieceType.load(world, nbtCompound);
                    structureStart.addPiece(structurePiece);
                    continue;
                }
                catch (Exception structurePiece) {
                    LOGGER.error("Exception loading structure piece with id {}", (Object)identifier2, (Object)structurePiece);
                }
            }
            return structureStart;
        }
        catch (Exception structureStart) {
            LOGGER.error("Failed Start with id {}", (Object)string, (Object)structureStart);
            return null;
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> getCodec() {
        return this.codec;
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configure(C config) {
        return new ConfiguredStructureFeature<C, StructureFeature>(this, config);
    }

    /**
     * Tries to find the closest structure of this type near a given block.
     * <p>
     * This method relies on the given world generation settings (seed and placement configuration)
     * to match the time at which the structure was generated, otherwise it will not be found.
     * <p>
     * New chunks will only be generated up to the {@link net.minecraft.world.chunk.ChunkStatus#STRUCTURE_STARTS} phase by this method.
     * 
     * @return {@code null} if no structure could be found within the given search radius
     * 
     * @param searchRadius the search radius in chunks around the chunk the given block position is in; a radius of 0 will only search in the given chunk
     */
    @Nullable
    public BlockPos locateStructure(WorldView world, StructureAccessor structureAccessor, BlockPos searchStartPos, int searchRadius, boolean skipExistingChunks, long worldSeed, StructureConfig config) {
        int i = config.getSpacing();
        int j = ChunkSectionPos.getSectionCoord(searchStartPos.getX());
        int k = ChunkSectionPos.getSectionCoord(searchStartPos.getZ());
        ChunkRandom chunkRandom = new ChunkRandom();
        block0: for (int l = 0; l <= searchRadius; ++l) {
            for (int m = -l; m <= l; ++m) {
                boolean bl = m == -l || m == l;
                for (int n = -l; n <= l; ++n) {
                    Chunk chunk;
                    StructureStart<?> structureStart;
                    boolean bl2;
                    boolean bl3 = bl2 = n == -l || n == l;
                    if (!bl && !bl2) continue;
                    int o = j + i * m;
                    int p = k + i * n;
                    ChunkPos chunkPos = this.getStartChunk(config, worldSeed, chunkRandom, o, p);
                    boolean bl32 = world.getBiomeAccess().getBiomeForNoiseGen(chunkPos).getGenerationSettings().hasStructureFeature(this);
                    if (bl32 && (structureStart = structureAccessor.getStructureStart(ChunkSectionPos.from(chunk = world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS)), this, chunk)) != null && structureStart.hasChildren()) {
                        if (skipExistingChunks && structureStart.isInExistingChunk()) {
                            structureStart.incrementReferences();
                            return structureStart.getBlockPos();
                        }
                        if (!skipExistingChunks) {
                            return structureStart.getBlockPos();
                        }
                    }
                    if (l == 0) break;
                }
                if (l == 0) continue block0;
            }
        }
        return null;
    }

    /**
     * If true, this structure's start position will be uniformly distributed within
     * a placement grid cell. If false, the structure's starting point will be biased
     * towards the center of the cell.
     */
    protected boolean isUniformDistribution() {
        return true;
    }

    /**
     * Determines the cell of the structure placement grid a chunk belongs to, and
     * returns the chunk within that cell, that this structure will actually be placed at.
     * <p>
     * If the {@link StructureConfig} uses a separation setting greater than 0, the
     * placement will be constrained to [0, spacing - separation] within the grid cell.
     * If a non-uniform distribution is used for placement, then this also moves
     * the center towards the origin.
     * 
     * @see #isUniformDistribution()
     */
    public final ChunkPos getStartChunk(StructureConfig config, long worldSeed, ChunkRandom placementRandom, int chunkX, int chunkY) {
        int n;
        int m;
        int i = config.getSpacing();
        int j = config.getSeparation();
        int k = Math.floorDiv(chunkX, i);
        int l = Math.floorDiv(chunkY, i);
        placementRandom.setRegionSeed(worldSeed, k, l, config.getSalt());
        if (this.isUniformDistribution()) {
            m = placementRandom.nextInt(i - j);
            n = placementRandom.nextInt(i - j);
        } else {
            m = (placementRandom.nextInt(i - j) + placementRandom.nextInt(i - j)) / 2;
            n = (placementRandom.nextInt(i - j) + placementRandom.nextInt(i - j)) / 2;
        }
        return new ChunkPos(k * i + m, l * i + n);
    }

    /**
     * Checks if this structure can <em>actually</em> be placed at a potential structure position determined via
     * {@link #getStartChunk}. Specific structures override this method to reduce the spawn probability or
     * restrict the spawn in some other way.
     */
    protected boolean shouldStartAt(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long worldSeed, ChunkRandom random, ChunkPos pos, Biome biome, ChunkPos chunkPos, C config, HeightLimitView world) {
        return true;
    }

    private StructureStart<C> createStart(ChunkPos pos, int i, long l) {
        return this.getStructureStartFactory().create(this, pos, i, l);
    }

    /**
     * Tries to place a starting point for this type of structure in the given chunk.
     * <p>
     * If this structure doesn't have a starting point in the chunk, {@link StructureStart#DEFAULT}
     * will be returned.
     */
    public StructureStart<?> tryPlaceStart(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator generator, BiomeSource biomeSource, StructureManager manager, long worldSeed, ChunkPos pos, Biome biome, int referenceCount, ChunkRandom random, StructureConfig structureConfig, C config, HeightLimitView world) {
        ChunkPos chunkPos = this.getStartChunk(structureConfig, worldSeed, random, pos.x, pos.z);
        if (pos.x == chunkPos.x && pos.z == chunkPos.z && this.shouldStartAt(generator, biomeSource, worldSeed, random, pos, biome, chunkPos, config, world)) {
            StructureStart<C> structureStart = this.createStart(pos, referenceCount, worldSeed);
            structureStart.init(dynamicRegistryManager, generator, manager, pos, biome, config, world);
            if (structureStart.hasChildren()) {
                return structureStart;
            }
        }
        return StructureStart.DEFAULT;
    }

    public abstract StructureStartFactory<C> getStructureStartFactory();

    public String getName() {
        return (String)STRUCTURES.inverse().get(this);
    }

    public Pool<SpawnSettings.SpawnEntry> getMonsterSpawns() {
        return SpawnSettings.EMPTY_ENTRY_POOL;
    }

    public Pool<SpawnSettings.SpawnEntry> getCreatureSpawns() {
        return SpawnSettings.EMPTY_ENTRY_POOL;
    }

    public Pool<SpawnSettings.SpawnEntry> getUndergroundWaterCreatureSpawns() {
        return SpawnSettings.EMPTY_ENTRY_POOL;
    }

    public static interface StructureStartFactory<C extends FeatureConfig> {
        public StructureStart<C> create(StructureFeature<C> var1, ChunkPos var2, int var3, long var4);
    }
}
