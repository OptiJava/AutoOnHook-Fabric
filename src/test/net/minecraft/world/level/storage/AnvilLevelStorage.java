/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.level.storage.AlphaChunkIo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.RegionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilLevelStorage {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String FILE_EXTENSION = ".mcr";

    static boolean convertLevel(LevelStorage.Session storageSession, ProgressListener progressListener) {
        progressListener.progressStagePercentage(0);
        ArrayList<File> list = Lists.newArrayList();
        ArrayList<File> list2 = Lists.newArrayList();
        ArrayList<File> list3 = Lists.newArrayList();
        File file = storageSession.getWorldDirectory(World.OVERWORLD);
        File file2 = storageSession.getWorldDirectory(World.NETHER);
        File file3 = storageSession.getWorldDirectory(World.END);
        LOGGER.info("Scanning folders...");
        AnvilLevelStorage.addRegionFiles(file, list);
        if (file2.exists()) {
            AnvilLevelStorage.addRegionFiles(file2, list2);
        }
        if (file3.exists()) {
            AnvilLevelStorage.addRegionFiles(file3, list3);
        }
        int i = list.size() + list2.size() + list3.size();
        LOGGER.info("Total conversion count is {}", (Object)i);
        DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
        RegistryOps<NbtElement> registryOps = RegistryOps.ofLoaded(NbtOps.INSTANCE, ResourceManager.Empty.INSTANCE, (DynamicRegistryManager)impl);
        SaveProperties saveProperties = storageSession.readLevelProperties(registryOps, DataPackSettings.SAFE_MODE);
        long l = saveProperties != null ? saveProperties.getGeneratorOptions().getSeed() : 0L;
        Registry<Biome> registry = impl.get(Registry.BIOME_KEY);
        BiomeSource biomeSource = saveProperties != null && saveProperties.getGeneratorOptions().isFlatWorld() ? new FixedBiomeSource(registry.getOrThrow(BiomeKeys.PLAINS)) : new VanillaLayeredBiomeSource(l, false, false, registry);
        AnvilLevelStorage.convertRegions(impl, new File(file, "region"), list, biomeSource, 0, i, progressListener);
        AnvilLevelStorage.convertRegions(impl, new File(file2, "region"), list2, new FixedBiomeSource(registry.getOrThrow(BiomeKeys.NETHER_WASTES)), list.size(), i, progressListener);
        AnvilLevelStorage.convertRegions(impl, new File(file3, "region"), list3, new FixedBiomeSource(registry.getOrThrow(BiomeKeys.THE_END)), list.size() + list2.size(), i, progressListener);
        AnvilLevelStorage.makeMcrLevelDatBackup(storageSession);
        storageSession.backupLevelDataFile(impl, saveProperties);
        return true;
    }

    private static void makeMcrLevelDatBackup(LevelStorage.Session storageSession) {
        File file = storageSession.getDirectory(WorldSavePath.LEVEL_DAT).toFile();
        if (!file.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
            return;
        }
        File file2 = new File(file.getParent(), "level.dat_mcr");
        if (!file.renameTo(file2)) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
        }
    }

    private static void convertRegions(DynamicRegistryManager.Impl registryManager, File directory, Iterable<File> files, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
        for (File file : files) {
            AnvilLevelStorage.convertRegion(registryManager, directory, file, biomeSource, i, j, progressListener);
            int k = (int)Math.round(100.0 * (double)(++i) / (double)j);
            progressListener.progressStagePercentage(k);
        }
    }

    private static void convertRegion(DynamicRegistryManager.Impl registryManager, File directory, File file, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
        String string = file.getName();
        try (RegionFile regionFile = new RegionFile(file, directory, true);
             RegionFile regionFile2 = new RegionFile(new File(directory, string.substring(0, string.length() - FILE_EXTENSION.length()) + ".mca"), directory, true);){
            for (int k = 0; k < 32; ++k) {
                int l;
                for (l = 0; l < 32; ++l) {
                    NbtCompound nbtCompound;
                    Object dataInputStream;
                    ChunkPos chunkPos = new ChunkPos(k, l);
                    if (!regionFile.hasChunk(chunkPos) || regionFile2.hasChunk(chunkPos)) continue;
                    try {
                        dataInputStream = regionFile.getChunkInputStream(chunkPos);
                        try {
                            if (dataInputStream == null) {
                                LOGGER.warn("Failed to fetch input stream for chunk {}", (Object)chunkPos);
                                continue;
                            }
                            nbtCompound = NbtIo.read((DataInput)dataInputStream);
                        }
                        finally {
                            if (dataInputStream != null) {
                                ((FilterInputStream)dataInputStream).close();
                            }
                        }
                    }
                    catch (IOException dataInputStream2) {
                        LOGGER.warn("Failed to read data for chunk {}", (Object)chunkPos, (Object)dataInputStream2);
                        continue;
                    }
                    dataInputStream = nbtCompound.getCompound("Level");
                    AlphaChunkIo.AlphaChunk alphaChunk = AlphaChunkIo.readAlphaChunk((NbtCompound)dataInputStream);
                    NbtCompound nbtCompound2 = new NbtCompound();
                    NbtCompound nbtCompound3 = new NbtCompound();
                    nbtCompound2.put("Level", nbtCompound3);
                    AlphaChunkIo.convertAlphaChunk(registryManager, alphaChunk, nbtCompound3, biomeSource);
                    try (DataOutputStream dataOutputStream = regionFile2.getChunkOutputStream(chunkPos);){
                        NbtIo.write(nbtCompound2, (DataOutput)dataOutputStream);
                        continue;
                    }
                }
                l = (int)Math.round(100.0 * (double)(i * 1024) / (double)(j * 1024));
                int chunkPos = (int)Math.round(100.0 * (double)((k + 1) * 32 + i * 1024) / (double)(j * 1024));
                if (chunkPos <= l) continue;
                progressListener.progressStagePercentage(chunkPos);
            }
        }
        catch (IOException regionFile3) {
            LOGGER.error("Failed to upgrade region file {}", (Object)file, (Object)regionFile3);
        }
    }

    private static void addRegionFiles(File worldDirectory, Collection<File> files) {
        File file = new File(worldDirectory, "region");
        File[] files2 = file.listFiles((directory, name) -> name.endsWith(FILE_EXTENSION));
        if (files2 != null) {
            Collections.addAll(files, files2);
        }
    }
}

