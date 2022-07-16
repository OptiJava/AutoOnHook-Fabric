/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.level.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.level.storage.AlphaChunkDataArray;

public class AlphaChunkIo {
    private static final int field_31416 = 7;
    private static final HeightLimitView WORLD = new HeightLimitView(){

        @Override
        public int getBottomY() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 128;
        }
    };

    public static AlphaChunk readAlphaChunk(NbtCompound nbt) {
        int i = nbt.getInt("xPos");
        int j = nbt.getInt("zPos");
        AlphaChunk alphaChunk = new AlphaChunk(i, j);
        alphaChunk.blocks = nbt.getByteArray("Blocks");
        alphaChunk.data = new AlphaChunkDataArray(nbt.getByteArray("Data"), 7);
        alphaChunk.skyLight = new AlphaChunkDataArray(nbt.getByteArray("SkyLight"), 7);
        alphaChunk.blockLight = new AlphaChunkDataArray(nbt.getByteArray("BlockLight"), 7);
        alphaChunk.heightMap = nbt.getByteArray("HeightMap");
        alphaChunk.terrainPopulated = nbt.getBoolean("TerrainPopulated");
        alphaChunk.entities = nbt.getList("Entities", 10);
        alphaChunk.blockEntities = nbt.getList("TileEntities", 10);
        alphaChunk.blockTicks = nbt.getList("TileTicks", 10);
        try {
            alphaChunk.lastUpdate = nbt.getLong("LastUpdate");
        }
        catch (ClassCastException classCastException) {
            alphaChunk.lastUpdate = nbt.getInt("LastUpdate");
        }
        return alphaChunk;
    }

    public static void convertAlphaChunk(DynamicRegistryManager.Impl impl, AlphaChunk alphaChunk, NbtCompound nbt, BiomeSource biomeSource) {
        nbt.putInt("xPos", alphaChunk.x);
        nbt.putInt("zPos", alphaChunk.z);
        nbt.putLong("LastUpdate", alphaChunk.lastUpdate);
        int[] is = new int[alphaChunk.heightMap.length];
        for (int i = 0; i < alphaChunk.heightMap.length; ++i) {
            is[i] = alphaChunk.heightMap[i];
        }
        nbt.putIntArray("HeightMap", is);
        nbt.putBoolean("TerrainPopulated", alphaChunk.terrainPopulated);
        NbtList i = new NbtList();
        for (int j = 0; j < 8; ++j) {
            int o;
            boolean bl = true;
            for (int k = 0; k < 16 && bl; ++k) {
                block3: for (int l = 0; l < 16 && bl; ++l) {
                    for (int m = 0; m < 16; ++m) {
                        int n = k << 11 | m << 7 | l + (j << 4);
                        o = alphaChunk.blocks[n];
                        if (o == 0) continue;
                        bl = false;
                        continue block3;
                    }
                }
            }
            if (bl) continue;
            byte[] k = new byte[4096];
            ChunkNibbleArray l = new ChunkNibbleArray();
            ChunkNibbleArray m = new ChunkNibbleArray();
            ChunkNibbleArray n = new ChunkNibbleArray();
            for (o = 0; o < 16; ++o) {
                for (int p = 0; p < 16; ++p) {
                    for (int q = 0; q < 16; ++q) {
                        int r = o << 11 | q << 7 | p + (j << 4);
                        byte s = alphaChunk.blocks[r];
                        k[p << 8 | q << 4 | o] = (byte)(s & 0xFF);
                        l.set(o, p, q, alphaChunk.data.get(o, p + (j << 4), q));
                        m.set(o, p, q, alphaChunk.skyLight.get(o, p + (j << 4), q));
                        n.set(o, p, q, alphaChunk.blockLight.get(o, p + (j << 4), q));
                    }
                }
            }
            NbtCompound o2 = new NbtCompound();
            o2.putByte("Y", (byte)(j & 0xFF));
            o2.putByteArray("Blocks", k);
            o2.putByteArray("Data", l.asByteArray());
            o2.putByteArray("SkyLight", m.asByteArray());
            o2.putByteArray("BlockLight", n.asByteArray());
            i.add(o2);
        }
        nbt.put("Sections", i);
        nbt.putIntArray("Biomes", new BiomeArray(impl.get(Registry.BIOME_KEY), WORLD, new ChunkPos(alphaChunk.x, alphaChunk.z), biomeSource).toIntArray());
        nbt.put("Entities", alphaChunk.entities);
        nbt.put("TileEntities", alphaChunk.blockEntities);
        if (alphaChunk.blockTicks != null) {
            nbt.put("TileTicks", alphaChunk.blockTicks);
        }
        nbt.putBoolean("convertedFromAlphaFormat", true);
    }

    public static class AlphaChunk {
        public long lastUpdate;
        public boolean terrainPopulated;
        public byte[] heightMap;
        public AlphaChunkDataArray blockLight;
        public AlphaChunkDataArray skyLight;
        public AlphaChunkDataArray data;
        public byte[] blocks;
        public NbtList entities;
        public NbtList blockEntities;
        public NbtList blockTicks;
        public final int x;
        public final int z;

        public AlphaChunk(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }
}

