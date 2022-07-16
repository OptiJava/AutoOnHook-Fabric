/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.chunk;

import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import org.jetbrains.annotations.Nullable;

public final class ChunkNibbleArray {
    public static final int COPY_TIMES = 16;
    public static final int COPY_BLOCK_SIZE = 128;
    public static final int BYTES_LENGTH = 2048;
    private static final int field_31405 = 4;
    @Nullable
    protected byte[] bytes;

    public ChunkNibbleArray() {
    }

    public ChunkNibbleArray(byte[] bytes) {
        this.bytes = bytes;
        if (bytes.length != 2048) {
            throw Util.throwOrPause(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + bytes.length));
        }
    }

    protected ChunkNibbleArray(int size) {
        this.bytes = new byte[size];
    }

    public int get(int x, int y, int z) {
        return this.get(ChunkNibbleArray.getIndex(x, y, z));
    }

    public void set(int x, int y, int z, int value) {
        this.set(ChunkNibbleArray.getIndex(x, y, z), value);
    }

    private static int getIndex(int i, int x, int y) {
        return x << 8 | y << 4 | i;
    }

    private int get(int index) {
        if (this.bytes == null) {
            return 0;
        }
        int i = ChunkNibbleArray.divideByTwo(index);
        int j = ChunkNibbleArray.isOdd(index);
        return this.bytes[i] >> 4 * j & 0xF;
    }

    private void set(int index, int value) {
        if (this.bytes == null) {
            this.bytes = new byte[2048];
        }
        int i = ChunkNibbleArray.divideByTwo(index);
        int j = ChunkNibbleArray.isOdd(index);
        int k = ~(15 << 4 * j);
        int l = (value & 0xF) << 4 * j;
        this.bytes[i] = (byte)(this.bytes[i] & k | l);
    }

    private static int isOdd(int i) {
        return i & 1;
    }

    private static int divideByTwo(int i) {
        return i >> 1;
    }

    public byte[] asByteArray() {
        if (this.bytes == null) {
            this.bytes = new byte[2048];
        }
        return this.bytes;
    }

    public ChunkNibbleArray copy() {
        if (this.bytes == null) {
            return new ChunkNibbleArray();
        }
        return new ChunkNibbleArray((byte[])this.bytes.clone());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4096; ++i) {
            stringBuilder.append(Integer.toHexString(this.get(i)));
            if ((i & 0xF) == 15) {
                stringBuilder.append("\n");
            }
            if ((i & 0xFF) != 255) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    @Debug
    public String method_35320(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < 256; ++j) {
            stringBuilder.append(Integer.toHexString(this.get(j)));
            if ((j & 0xF) != 15) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public boolean isUninitialized() {
        return this.bytes == null;
    }
}

