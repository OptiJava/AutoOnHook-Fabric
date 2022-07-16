/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.light.LevelPropagator;

public abstract class ChunkPosDistanceLevelPropagator
extends LevelPropagator {
    protected ChunkPosDistanceLevelPropagator(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected boolean isMarker(long id) {
        return id == ChunkPos.MARKER;
    }

    @Override
    protected void propagateLevel(long id, int level, boolean decrease) {
        ChunkPos chunkPos = new ChunkPos(id);
        int i = chunkPos.x;
        int j = chunkPos.z;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                long m = ChunkPos.toLong(i + k, j + l);
                if (m == id) continue;
                this.propagateLevel(id, m, level, decrease);
            }
        }
    }

    @Override
    protected int recalculateLevel(long id, long excludedId, int maxLevel) {
        int i = maxLevel;
        ChunkPos chunkPos = new ChunkPos(id);
        int j = chunkPos.x;
        int k = chunkPos.z;
        for (int l = -1; l <= 1; ++l) {
            for (int m = -1; m <= 1; ++m) {
                long n = ChunkPos.toLong(j + l, k + m);
                if (n == id) {
                    n = ChunkPos.MARKER;
                }
                if (n == excludedId) continue;
                int o = this.getPropagatedLevel(n, id, this.getLevel(n));
                if (i > o) {
                    i = o;
                }
                if (i != 0) continue;
                return i;
            }
        }
        return i;
    }

    @Override
    protected int getPropagatedLevel(long sourceId, long targetId, int level) {
        if (sourceId == ChunkPos.MARKER) {
            return this.getInitialLevel(targetId);
        }
        return level + 1;
    }

    protected abstract int getInitialLevel(long var1);

    public void updateLevel(long chunkPos, int distance, boolean decrease) {
        this.updateLevel(ChunkPos.MARKER, chunkPos, distance, decrease);
    }
}

