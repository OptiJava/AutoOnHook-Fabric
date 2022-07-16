/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ChunkSerializer {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String field_31413 = "UpgradeData";

    public static ProtoChunk deserialize(ServerWorld world, StructureManager structureManager, PointOfInterestStorage poiStorage, ChunkPos pos, NbtCompound nbt) {
        int l;
        NbtList nbtList2;
        Object tickScheduler2;
        TickScheduler<Block> tickScheduler;
        Object chunkSection;
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        BiomeSource biomeSource = chunkGenerator.getBiomeSource();
        NbtCompound nbtCompound = nbt.getCompound("Level");
        ChunkPos chunkPos = new ChunkPos(nbtCompound.getInt("xPos"), nbtCompound.getInt("zPos"));
        if (!Objects.equals(pos, chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", (Object)pos, (Object)pos, (Object)chunkPos);
        }
        BiomeArray biomeArray = new BiomeArray(world.getRegistryManager().get(Registry.BIOME_KEY), world, pos, biomeSource, nbtCompound.contains("Biomes", 11) ? nbtCompound.getIntArray("Biomes") : null);
        UpgradeData upgradeData = nbtCompound.contains(field_31413, 10) ? new UpgradeData(nbtCompound.getCompound(field_31413), world) : UpgradeData.NO_UPGRADE_DATA;
        ChunkTickScheduler<Block> chunkTickScheduler = new ChunkTickScheduler<Block>(block -> block == null || block.getDefaultState().isAir(), pos, nbtCompound.getList("ToBeTicked", 9), world);
        ChunkTickScheduler<Fluid> chunkTickScheduler2 = new ChunkTickScheduler<Fluid>(fluid -> fluid == null || fluid == Fluids.EMPTY, pos, nbtCompound.getList("LiquidsToBeTicked", 9), world);
        boolean bl = nbtCompound.getBoolean("isLightOn");
        NbtList nbtList = nbtCompound.getList("Sections", 10);
        int i = world.countVerticalSections();
        ChunkSection[] chunkSections = new ChunkSection[i];
        boolean bl2 = world.getDimension().hasSkyLight();
        ServerChunkManager chunkManager = world.getChunkManager();
        LightingProvider lightingProvider = ((ChunkManager)chunkManager).getLightingProvider();
        if (bl) {
            lightingProvider.setRetainData(pos, true);
        }
        for (int j = 0; j < nbtList.size(); ++j) {
            NbtCompound nbtCompound2 = nbtList.getCompound(j);
            byte k = nbtCompound2.getByte("Y");
            if (nbtCompound2.contains("Palette", 9) && nbtCompound2.contains("BlockStates", 12)) {
                chunkSection = new ChunkSection(k);
                ((ChunkSection)chunkSection).getContainer().read(nbtCompound2.getList("Palette", 10), nbtCompound2.getLongArray("BlockStates"));
                ((ChunkSection)chunkSection).calculateCounts();
                if (!((ChunkSection)chunkSection).isEmpty()) {
                    chunkSections[world.sectionCoordToIndex((int)k)] = chunkSection;
                }
                poiStorage.initForPalette(pos, (ChunkSection)chunkSection);
            }
            if (!bl) continue;
            if (nbtCompound2.contains("BlockLight", 7)) {
                lightingProvider.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(pos, k), new ChunkNibbleArray(nbtCompound2.getByteArray("BlockLight")), true);
            }
            if (!bl2 || !nbtCompound2.contains("SkyLight", 7)) continue;
            lightingProvider.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(pos, k), new ChunkNibbleArray(nbtCompound2.getByteArray("SkyLight")), true);
        }
        long j = nbtCompound.getLong("InhabitedTime");
        ChunkStatus.ChunkType k = ChunkSerializer.getChunkType(nbt);
        if (k == ChunkStatus.ChunkType.LEVELCHUNK) {
            tickScheduler = nbtCompound.contains("TileTicks", 9) ? SimpleTickScheduler.fromNbt(nbtCompound.getList("TileTicks", 10), Registry.BLOCK::getId, Registry.BLOCK::get) : chunkTickScheduler;
            tickScheduler2 = nbtCompound.contains("LiquidTicks", 9) ? SimpleTickScheduler.fromNbt(nbtCompound.getList("LiquidTicks", 10), Registry.FLUID::getId, Registry.FLUID::get) : chunkTickScheduler2;
            chunkSection = new WorldChunk(world.toServerWorld(), pos, biomeArray, upgradeData, tickScheduler, (TickScheduler<Fluid>)tickScheduler2, j, chunkSections, worldChunk -> ChunkSerializer.loadEntities(world, nbtCompound, worldChunk));
        } else {
            tickScheduler = new ProtoChunk(pos, upgradeData, chunkSections, chunkTickScheduler, chunkTickScheduler2, world);
            ((ProtoChunk)((Object)tickScheduler)).setBiomes(biomeArray);
            chunkSection = tickScheduler;
            chunkSection.setInhabitedTime(j);
            ((ProtoChunk)((Object)tickScheduler)).setStatus(ChunkStatus.byId(nbtCompound.getString("Status")));
            if (chunkSection.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
                ((ProtoChunk)((Object)tickScheduler)).setLightingProvider(lightingProvider);
            }
            if (!bl && chunkSection.getStatus().isAtLeast(ChunkStatus.LIGHT)) {
                for (BlockPos blockPos : BlockPos.iterate(pos.getStartX(), world.getBottomY(), pos.getStartZ(), pos.getEndX(), world.getTopY() - 1, pos.getEndZ())) {
                    if (chunkSection.getBlockState(blockPos).getLuminance() == 0) continue;
                    ((ProtoChunk)((Object)tickScheduler)).addLightSource(blockPos);
                }
            }
        }
        chunkSection.setLightOn(bl);
        tickScheduler = nbtCompound.getCompound("Heightmaps");
        tickScheduler2 = EnumSet.noneOf(Heightmap.Type.class);
        for (Heightmap.Type type : chunkSection.getStatus().getHeightmapTypes()) {
            String string = type.getName();
            if (((NbtCompound)((Object)tickScheduler)).contains(string, 12)) {
                chunkSection.setHeightmap(type, ((NbtCompound)((Object)tickScheduler)).getLongArray(string));
                continue;
            }
            ((AbstractCollection)tickScheduler2).add((Heightmap.Type)type);
        }
        Heightmap.populateHeightmaps((Chunk)chunkSection, (Set<Heightmap.Type>)tickScheduler2);
        NbtCompound nbtCompound2 = nbtCompound.getCompound("Structures");
        chunkSection.setStructureStarts(ChunkSerializer.readStructureStarts(world, nbtCompound2, world.getSeed()));
        chunkSection.setStructureReferences(ChunkSerializer.readStructureReferences(pos, nbtCompound2));
        if (nbtCompound.getBoolean("shouldSave")) {
            chunkSection.setShouldSave(true);
        }
        NbtList nbtList3 = nbtCompound.getList("PostProcessing", 9);
        for (int string = 0; string < nbtList3.size(); ++string) {
            nbtList2 = nbtList3.getList(string);
            for (l = 0; l < nbtList2.size(); ++l) {
                chunkSection.markBlockForPostProcessing(nbtList2.getShort(l), string);
            }
        }
        if (k == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ReadOnlyChunk((WorldChunk)chunkSection);
        }
        ProtoChunk string = (ProtoChunk)chunkSection;
        nbtList2 = nbtCompound.getList("Entities", 10);
        for (l = 0; l < nbtList2.size(); ++l) {
            string.addEntity(nbtList2.getCompound(l));
        }
        NbtList l2 = nbtCompound.getList("TileEntities", 10);
        for (int m = 0; m < l2.size(); ++m) {
            NbtCompound nbtCompound3 = l2.getCompound(m);
            chunkSection.addPendingBlockEntityNbt(nbtCompound3);
        }
        NbtList m = nbtCompound.getList("Lights", 9);
        for (int nbtCompound3 = 0; nbtCompound3 < m.size(); ++nbtCompound3) {
            NbtList nbtList32 = m.getList(nbtCompound3);
            for (int n = 0; n < nbtList32.size(); ++n) {
                string.addLightSource(nbtList32.getShort(n), nbtCompound3);
            }
        }
        NbtCompound nbtCompound3 = nbtCompound.getCompound("CarvingMasks");
        for (String n : nbtCompound3.getKeys()) {
            GenerationStep.Carver carver = GenerationStep.Carver.valueOf(n);
            string.setCarvingMask(carver, BitSet.valueOf(nbtCompound3.getByteArray(n)));
        }
        return string;
    }

    public static NbtCompound serialize(ServerWorld world, Chunk chunk) {
        Object chunkSection2;
        BiomeArray i;
        Object chunkNibbleArray2;
        Object chunkNibbleArray3;
        ChunkPos chunkPos = chunk.getPos();
        NbtCompound nbtCompound = new NbtCompound();
        NbtCompound nbtCompound2 = new NbtCompound();
        nbtCompound.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
        nbtCompound.put("Level", nbtCompound2);
        nbtCompound2.putInt("xPos", chunkPos.x);
        nbtCompound2.putInt("zPos", chunkPos.z);
        nbtCompound2.putLong("LastUpdate", world.getTime());
        nbtCompound2.putLong("InhabitedTime", chunk.getInhabitedTime());
        nbtCompound2.putString("Status", chunk.getStatus().getId());
        UpgradeData upgradeData = chunk.getUpgradeData();
        if (!upgradeData.isDone()) {
            nbtCompound2.put(field_31413, upgradeData.toNbt());
        }
        ChunkSection[] chunkSections = chunk.getSectionArray();
        NbtList nbtList = new NbtList();
        ServerLightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
        boolean bl = chunk.isLightOn();
        for (int i2 = lightingProvider.getBottomY(); i2 < lightingProvider.getTopY(); ++i2) {
            int j = i2;
            ChunkSection chunkSection22 = Arrays.stream(chunkSections).filter(chunkSection -> chunkSection != null && ChunkSectionPos.getSectionCoord(chunkSection.getYOffset()) == j).findFirst().orElse(WorldChunk.EMPTY_SECTION);
            chunkNibbleArray3 = lightingProvider.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(chunkPos, j));
            chunkNibbleArray2 = lightingProvider.get(LightType.SKY).getLightSection(ChunkSectionPos.from(chunkPos, j));
            if (chunkSection22 == WorldChunk.EMPTY_SECTION && chunkNibbleArray3 == null && chunkNibbleArray2 == null) continue;
            NbtCompound nbtCompound3 = new NbtCompound();
            nbtCompound3.putByte("Y", (byte)(j & 0xFF));
            if (chunkSection22 != WorldChunk.EMPTY_SECTION) {
                chunkSection22.getContainer().write(nbtCompound3, "Palette", "BlockStates");
            }
            if (chunkNibbleArray3 != null && !((ChunkNibbleArray)chunkNibbleArray3).isUninitialized()) {
                nbtCompound3.putByteArray("BlockLight", ((ChunkNibbleArray)chunkNibbleArray3).asByteArray());
            }
            if (chunkNibbleArray2 != null && !((ChunkNibbleArray)chunkNibbleArray2).isUninitialized()) {
                nbtCompound3.putByteArray("SkyLight", ((ChunkNibbleArray)chunkNibbleArray2).asByteArray());
            }
            nbtList.add(nbtCompound3);
        }
        nbtCompound2.put("Sections", nbtList);
        if (bl) {
            nbtCompound2.putBoolean("isLightOn", true);
        }
        if ((i = chunk.getBiomeArray()) != null) {
            nbtCompound2.putIntArray("Biomes", i.toIntArray());
        }
        NbtList j = new NbtList();
        for (Object chunkNibbleArray3 : chunk.getBlockEntityPositions()) {
            chunkNibbleArray2 = chunk.getPackedBlockEntityNbt((BlockPos)chunkNibbleArray3);
            if (chunkNibbleArray2 == null) continue;
            j.add(chunkNibbleArray2);
        }
        nbtCompound2.put("TileEntities", j);
        if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            chunkSection2 = (ProtoChunk)chunk;
            chunkNibbleArray3 = new NbtList();
            ((AbstractCollection)chunkNibbleArray3).addAll(((ProtoChunk)chunkSection2).getEntities());
            nbtCompound2.put("Entities", (NbtElement)chunkNibbleArray3);
            nbtCompound2.put("Lights", ChunkSerializer.toNbt(((ProtoChunk)chunkSection2).getLightSourcesBySection()));
            chunkNibbleArray2 = new NbtCompound();
            for (GenerationStep.Carver carver : GenerationStep.Carver.values()) {
                BitSet bitSet = ((ProtoChunk)chunkSection2).getCarvingMask(carver);
                if (bitSet == null) continue;
                ((NbtCompound)chunkNibbleArray2).putByteArray(carver.toString(), bitSet.toByteArray());
            }
            nbtCompound2.put("CarvingMasks", (NbtElement)chunkNibbleArray2);
        }
        if ((chunkSection2 = chunk.getBlockTickScheduler()) instanceof ChunkTickScheduler) {
            nbtCompound2.put("ToBeTicked", ((ChunkTickScheduler)chunkSection2).toNbt());
        } else if (chunkSection2 instanceof SimpleTickScheduler) {
            nbtCompound2.put("TileTicks", ((SimpleTickScheduler)chunkSection2).toNbt());
        } else {
            nbtCompound2.put("TileTicks", ((ServerTickScheduler)world.getBlockTickScheduler()).toNbt(chunkPos));
        }
        chunkNibbleArray3 = chunk.getFluidTickScheduler();
        if (chunkNibbleArray3 instanceof ChunkTickScheduler) {
            nbtCompound2.put("LiquidsToBeTicked", ((ChunkTickScheduler)chunkNibbleArray3).toNbt());
        } else if (chunkNibbleArray3 instanceof SimpleTickScheduler) {
            nbtCompound2.put("LiquidTicks", ((SimpleTickScheduler)chunkNibbleArray3).toNbt());
        } else {
            nbtCompound2.put("LiquidTicks", ((ServerTickScheduler)world.getFluidTickScheduler()).toNbt(chunkPos));
        }
        nbtCompound2.put("PostProcessing", ChunkSerializer.toNbt(chunk.getPostProcessingLists()));
        chunkNibbleArray2 = new NbtCompound();
        for (Map.Entry entry : chunk.getHeightmaps()) {
            if (!chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) continue;
            ((NbtCompound)chunkNibbleArray2).put(((Heightmap.Type)entry.getKey()).getName(), new NbtLongArray(((Heightmap)entry.getValue()).asLongArray()));
        }
        nbtCompound2.put("Heightmaps", (NbtElement)chunkNibbleArray2);
        nbtCompound2.put("Structures", ChunkSerializer.writeStructures(world, chunkPos, chunk.getStructureStarts(), chunk.getStructureReferences()));
        return nbtCompound;
    }

    public static ChunkStatus.ChunkType getChunkType(@Nullable NbtCompound nbt) {
        ChunkStatus chunkStatus;
        if (nbt != null && (chunkStatus = ChunkStatus.byId(nbt.getCompound("Level").getString("Status"))) != null) {
            return chunkStatus.getChunkType();
        }
        return ChunkStatus.ChunkType.PROTOCHUNK;
    }

    private static void loadEntities(ServerWorld world, NbtCompound nbt, WorldChunk chunk) {
        NbtList nbtList;
        if (nbt.contains("Entities", 9) && !(nbtList = nbt.getList("Entities", 10)).isEmpty()) {
            world.loadEntities(EntityType.streamFromNbt(nbtList, world));
        }
        nbtList = nbt.getList("TileEntities", 10);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            boolean bl = nbtCompound.getBoolean("keepPacked");
            if (bl) {
                chunk.addPendingBlockEntityNbt(nbtCompound);
                continue;
            }
            BlockPos blockPos = new BlockPos(nbtCompound.getInt("x"), nbtCompound.getInt("y"), nbtCompound.getInt("z"));
            BlockEntity blockEntity = BlockEntity.createFromNbt(blockPos, chunk.getBlockState(blockPos), nbtCompound);
            if (blockEntity == null) continue;
            chunk.setBlockEntity(blockEntity);
        }
    }

    private static NbtCompound writeStructures(ServerWorld world, ChunkPos chunkPos, Map<StructureFeature<?>, StructureStart<?>> map, Map<StructureFeature<?>, LongSet> map2) {
        NbtCompound nbtCompound = new NbtCompound();
        NbtCompound nbtCompound2 = new NbtCompound();
        for (Map.Entry<StructureFeature<?>, StructureStart<?>> entry : map.entrySet()) {
            nbtCompound2.put(entry.getKey().getName(), entry.getValue().toNbt(world, chunkPos));
        }
        nbtCompound.put("Starts", nbtCompound2);
        NbtCompound nbtCompound3 = new NbtCompound();
        for (Map.Entry<StructureFeature<?>, LongSet> entry2 : map2.entrySet()) {
            nbtCompound3.put(entry2.getKey().getName(), new NbtLongArray(entry2.getValue()));
        }
        nbtCompound.put("References", nbtCompound3);
        return nbtCompound;
    }

    private static Map<StructureFeature<?>, StructureStart<?>> readStructureStarts(ServerWorld serverWorld, NbtCompound nbt, long worldSeed) {
        HashMap<StructureFeature<?>, StructureStart<?>> map = Maps.newHashMap();
        NbtCompound nbtCompound = nbt.getCompound("Starts");
        for (String string : nbtCompound.getKeys()) {
            String string2 = string.toLowerCase(Locale.ROOT);
            StructureFeature structureFeature = (StructureFeature)StructureFeature.STRUCTURES.get(string2);
            if (structureFeature == null) {
                LOGGER.error("Unknown structure start: {}", (Object)string2);
                continue;
            }
            StructureStart<?> structureStart = StructureFeature.readStructureStart(serverWorld, nbtCompound.getCompound(string), worldSeed);
            if (structureStart == null) continue;
            map.put(structureFeature, structureStart);
        }
        return map;
    }

    private static Map<StructureFeature<?>, LongSet> readStructureReferences(ChunkPos pos, NbtCompound nbt) {
        HashMap<StructureFeature<?>, LongSet> map = Maps.newHashMap();
        NbtCompound nbtCompound = nbt.getCompound("References");
        for (String string : nbtCompound.getKeys()) {
            String string2 = string.toLowerCase(Locale.ROOT);
            StructureFeature structureFeature = (StructureFeature)StructureFeature.STRUCTURES.get(string2);
            if (structureFeature == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)string2, (Object)pos);
                continue;
            }
            map.put(structureFeature, new LongOpenHashSet(Arrays.stream(nbtCompound.getLongArray(string)).filter(packedPos -> {
                ChunkPos chunkPos2 = new ChunkPos(packedPos);
                if (chunkPos2.getChebyshevDistance(pos) > 8) {
                    LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", (Object)string2, (Object)chunkPos2, (Object)pos);
                    return false;
                }
                return true;
            }).toArray()));
        }
        return map;
    }

    public static NbtList toNbt(ShortList[] lists) {
        NbtList nbtList = new NbtList();
        for (ShortList shortList : lists) {
            NbtList nbtList2 = new NbtList();
            if (shortList != null) {
                for (Short short_ : shortList) {
                    nbtList2.add(NbtShort.of(short_));
                }
            }
            nbtList.add(nbtList2);
        }
        return nbtList;
    }
}

