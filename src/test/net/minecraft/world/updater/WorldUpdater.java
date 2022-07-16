/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.updater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUpdater {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadFactory UPDATE_THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private final ImmutableSet<RegistryKey<World>> worlds;
    private final boolean eraseCache;
    private final LevelStorage.Session session;
    private final Thread updateThread;
    private final DataFixer dataFixer;
    private volatile boolean keepUpgradingChunks = true;
    private volatile boolean done;
    private volatile float progress;
    private volatile int totalChunkCount;
    private volatile int upgradedChunkCount;
    private volatile int skippedChunkCount;
    private final Object2FloatMap<RegistryKey<World>> dimensionProgress = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap(Util.identityHashStrategy()));
    private volatile Text status = new TranslatableText("optimizeWorld.stage.counting");
    private static final Pattern REGION_FILE_PATTERN = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final PersistentStateManager persistentStateManager;

    public WorldUpdater(LevelStorage.Session session, DataFixer dataFixer, ImmutableSet<RegistryKey<World>> worlds, boolean eraseCache) {
        this.worlds = worlds;
        this.eraseCache = eraseCache;
        this.dataFixer = dataFixer;
        this.session = session;
        this.persistentStateManager = new PersistentStateManager(new File(this.session.getWorldDirectory(World.OVERWORLD), "data"), dataFixer);
        this.updateThread = UPDATE_THREAD_FACTORY.newThread(this::updateWorld);
        this.updateThread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Error upgrading world", throwable);
            this.status = new TranslatableText("optimizeWorld.stage.failed");
            this.done = true;
        });
        this.updateThread.start();
    }

    public void cancel() {
        this.keepUpgradingChunks = false;
        try {
            this.updateThread.join();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void updateWorld() {
        Object list;
        this.totalChunkCount = 0;
        ImmutableMap.Builder<RegistryKey, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
        for (RegistryKey registryKey : this.worlds) {
            list = this.getChunkPositions(registryKey);
            builder.put(registryKey, list.listIterator());
            this.totalChunkCount += list.size();
        }
        if (this.totalChunkCount == 0) {
            this.done = true;
            return;
        }
        float f = this.totalChunkCount;
        ImmutableMap immutableMap = builder.build();
        list = ImmutableMap.builder();
        for (RegistryKey registryKey : this.worlds) {
            File file = this.session.getWorldDirectory(registryKey);
            ((ImmutableMap.Builder)list).put(registryKey, new VersionedChunkStorage(new File(file, "region"), this.dataFixer, true));
        }
        ImmutableMap immutableMap2 = ((ImmutableMap.Builder)list).build();
        long l = Util.getMeasuringTimeMs();
        this.status = new TranslatableText("optimizeWorld.stage.upgrading");
        while (this.keepUpgradingChunks) {
            boolean bl = false;
            float g = 0.0f;
            for (RegistryKey registryKey : this.worlds) {
                ListIterator listIterator = (ListIterator)immutableMap.get(registryKey);
                VersionedChunkStorage versionedChunkStorage = (VersionedChunkStorage)immutableMap2.get(registryKey);
                if (listIterator.hasNext()) {
                    ChunkPos chunkPos = (ChunkPos)listIterator.next();
                    boolean bl2 = false;
                    try {
                        NbtCompound nbtCompound = versionedChunkStorage.getNbt(chunkPos);
                        if (nbtCompound != null) {
                            boolean bl3;
                            int i = VersionedChunkStorage.getDataVersion(nbtCompound);
                            NbtCompound nbtCompound2 = versionedChunkStorage.updateChunkNbt(registryKey, () -> this.persistentStateManager, nbtCompound);
                            NbtCompound nbtCompound3 = nbtCompound2.getCompound("Level");
                            ChunkPos chunkPos2 = new ChunkPos(nbtCompound3.getInt("xPos"), nbtCompound3.getInt("zPos"));
                            if (!chunkPos2.equals(chunkPos)) {
                                LOGGER.warn("Chunk {} has invalid position {}", (Object)chunkPos, (Object)chunkPos2);
                            }
                            boolean bl4 = bl3 = i < SharedConstants.getGameVersion().getWorldVersion();
                            if (this.eraseCache) {
                                bl3 = bl3 || nbtCompound3.contains("Heightmaps");
                                nbtCompound3.remove("Heightmaps");
                                bl3 = bl3 || nbtCompound3.contains("isLightOn");
                                nbtCompound3.remove("isLightOn");
                            }
                            if (bl3) {
                                versionedChunkStorage.setNbt(chunkPos, nbtCompound2);
                                bl2 = true;
                            }
                        }
                    }
                    catch (CrashException nbtCompound) {
                        Throwable i = nbtCompound.getCause();
                        if (i instanceof IOException) {
                            LOGGER.error("Error upgrading chunk {}", (Object)chunkPos, (Object)i);
                        }
                        throw nbtCompound;
                    }
                    catch (IOException nbtCompound) {
                        LOGGER.error("Error upgrading chunk {}", (Object)chunkPos, (Object)nbtCompound);
                    }
                    if (bl2) {
                        ++this.upgradedChunkCount;
                    } else {
                        ++this.skippedChunkCount;
                    }
                    bl = true;
                }
                float chunkPos = (float)listIterator.nextIndex() / f;
                this.dimensionProgress.put((RegistryKey<World>)registryKey, chunkPos);
                g += chunkPos;
            }
            this.progress = g;
            if (bl) continue;
            this.keepUpgradingChunks = false;
        }
        this.status = new TranslatableText("optimizeWorld.stage.finished");
        for (VersionedChunkStorage g : immutableMap2.values()) {
            try {
                g.close();
            }
            catch (IOException iOException) {
                LOGGER.error("Error upgrading chunk", (Throwable)iOException);
            }
        }
        this.persistentStateManager.save();
        l = Util.getMeasuringTimeMs() - l;
        LOGGER.info("World optimizaton finished after {} ms", (Object)l);
        this.done = true;
    }

    private List<ChunkPos> getChunkPositions(RegistryKey<World> world) {
        File file = this.session.getWorldDirectory(world);
        File file2 = new File(file, "region");
        File[] files = file2.listFiles((directory, name) -> name.endsWith(".mca"));
        if (files == null) {
            return ImmutableList.of();
        }
        ArrayList<ChunkPos> list = Lists.newArrayList();
        for (File file3 : files) {
            Matcher matcher = REGION_FILE_PATTERN.matcher(file3.getName());
            if (!matcher.matches()) continue;
            int i = Integer.parseInt(matcher.group(1)) << 5;
            int j = Integer.parseInt(matcher.group(2)) << 5;
            try (RegionFile regionFile = new RegionFile(file3, file2, true);){
                for (int k = 0; k < 32; ++k) {
                    for (int l = 0; l < 32; ++l) {
                        ChunkPos chunkPos = new ChunkPos(k + i, l + j);
                        if (!regionFile.isChunkValid(chunkPos)) continue;
                        list.add(chunkPos);
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return list;
    }

    public boolean isDone() {
        return this.done;
    }

    public ImmutableSet<RegistryKey<World>> getWorlds() {
        return this.worlds;
    }

    public float getProgress(RegistryKey<World> world) {
        return this.dimensionProgress.getFloat(world);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunkCount() {
        return this.totalChunkCount;
    }

    public int getUpgradedChunkCount() {
        return this.upgradedChunkCount;
    }

    public int getSkippedChunkCount() {
        return this.skippedChunkCount;
    }

    public Text getStatus() {
        return this.status;
    }
}

