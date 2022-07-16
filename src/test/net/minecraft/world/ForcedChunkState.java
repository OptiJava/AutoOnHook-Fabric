/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class ForcedChunkState
extends PersistentState {
    public static final String field_30961 = "chunks";
    private static final String FORCED_KEY = "Forced";
    private final LongSet chunks;

    private ForcedChunkState(LongSet chunks) {
        this.chunks = chunks;
    }

    public ForcedChunkState() {
        this(new LongOpenHashSet());
    }

    public static ForcedChunkState fromNbt(NbtCompound nbt) {
        return new ForcedChunkState(new LongOpenHashSet(nbt.getLongArray(FORCED_KEY)));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putLongArray(FORCED_KEY, this.chunks.toLongArray());
        return nbt;
    }

    public LongSet getChunks() {
        return this.chunks;
    }
}

