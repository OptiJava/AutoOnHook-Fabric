/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.chunk;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.thread.AtomicStack;
import net.minecraft.util.thread.LockHelper;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.jetbrains.annotations.Nullable;

public class PalettedContainer<T>
implements PaletteResizeListener<T> {
    private static final int field_31411 = 4096;
    public static final int field_31409 = 9;
    public static final int field_31410 = 4;
    private final Palette<T> fallbackPalette;
    private final PaletteResizeListener<T> noOpPaletteResizeHandler = (newSize, added) -> 0;
    private final IdList<T> idList;
    private final Function<NbtCompound, T> elementDeserializer;
    private final Function<T, NbtCompound> elementSerializer;
    private final T defaultValue;
    protected PackedIntegerArray data;
    private Palette<T> palette;
    private int paletteSize;
    private final Semaphore writeLock = new Semaphore(1);
    @Nullable
    private final AtomicStack<Pair<Thread, StackTraceElement[]>> lockStack = null;

    public void lock() {
        if (this.lockStack != null) {
            Thread thread = Thread.currentThread();
            this.lockStack.push(Pair.of(thread, thread.getStackTrace()));
        }
        LockHelper.checkLock(this.writeLock, this.lockStack, "PalettedContainer");
    }

    public void unlock() {
        this.writeLock.release();
    }

    public PalettedContainer(Palette<T> fallbackPalette, IdList<T> idList, Function<NbtCompound, T> elementDeserializer, Function<T, NbtCompound> elementSerializer, T defaultElement) {
        this.fallbackPalette = fallbackPalette;
        this.idList = idList;
        this.elementDeserializer = elementDeserializer;
        this.elementSerializer = elementSerializer;
        this.defaultValue = defaultElement;
        this.setPaletteSize(4);
    }

    private static int toIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    private void setPaletteSize(int size) {
        if (size == this.paletteSize) {
            return;
        }
        this.paletteSize = size;
        if (this.paletteSize <= 4) {
            this.paletteSize = 4;
            this.palette = new ArrayPalette<T>(this.idList, this.paletteSize, this, this.elementDeserializer);
        } else if (this.paletteSize < 9) {
            this.palette = new BiMapPalette<T>(this.idList, this.paletteSize, this, this.elementDeserializer, this.elementSerializer);
        } else {
            this.palette = this.fallbackPalette;
            this.paletteSize = MathHelper.log2DeBruijn(this.idList.size());
        }
        this.palette.getIndex(this.defaultValue);
        this.data = new PackedIntegerArray(this.paletteSize, 4096);
    }

    @Override
    public int onResize(int i, T object) {
        PackedIntegerArray packedIntegerArray = this.data;
        Palette<T> palette = this.palette;
        this.setPaletteSize(i);
        for (int j = 0; j < packedIntegerArray.getSize(); ++j) {
            T object2 = palette.getByIndex(packedIntegerArray.get(j));
            if (object2 == null) continue;
            this.set(j, object2);
        }
        return this.palette.getIndex(object);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T setSync(int x, int y, int z, T value) {
        try {
            T object;
            this.lock();
            T t = object = this.setAndGetOldValue(PalettedContainer.toIndex(x, y, z), value);
            return t;
        }
        finally {
            this.unlock();
        }
    }

    public T set(int x, int y, int z, T value) {
        return this.setAndGetOldValue(PalettedContainer.toIndex(x, y, z), value);
    }

    private T setAndGetOldValue(int index, T value) {
        int i = this.palette.getIndex(value);
        int j = this.data.setAndGetOldValue(index, i);
        T object = this.palette.getByIndex(j);
        return object == null ? this.defaultValue : object;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_35321(int i, int j, int k, T object) {
        try {
            this.lock();
            this.set(PalettedContainer.toIndex(i, j, k), object);
        }
        finally {
            this.unlock();
        }
    }

    private void set(int index, T object) {
        int i = this.palette.getIndex(object);
        this.data.set(index, i);
    }

    public T get(int x, int y, int z) {
        return this.get(PalettedContainer.toIndex(x, y, z));
    }

    protected T get(int index) {
        T object = this.palette.getByIndex(this.data.get(index));
        return object == null ? this.defaultValue : object;
    }

    public void fromPacket(PacketByteBuf buf) {
        try {
            this.lock();
            byte i = buf.readByte();
            if (this.paletteSize != i) {
                this.setPaletteSize(i);
            }
            this.palette.fromPacket(buf);
            buf.readLongArray(this.data.getStorage());
        }
        finally {
            this.unlock();
        }
    }

    public void toPacket(PacketByteBuf buf) {
        try {
            this.lock();
            buf.writeByte(this.paletteSize);
            this.palette.toPacket(buf);
            buf.writeLongArray(this.data.getStorage());
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void read(NbtList paletteNbt, long[] data) {
        try {
            this.lock();
            int i = Math.max(4, MathHelper.log2DeBruijn(paletteNbt.size()));
            if (i != this.paletteSize) {
                this.setPaletteSize(i);
            }
            this.palette.readNbt(paletteNbt);
            int j = data.length * 64 / 4096;
            if (this.palette == this.fallbackPalette) {
                BiMapPalette<T> palette = new BiMapPalette<T>(this.idList, i, this.noOpPaletteResizeHandler, this.elementDeserializer, this.elementSerializer);
                palette.readNbt(paletteNbt);
                PackedIntegerArray packedIntegerArray = new PackedIntegerArray(i, 4096, data);
                for (int k = 0; k < 4096; ++k) {
                    this.data.set(k, this.fallbackPalette.getIndex(palette.getByIndex(packedIntegerArray.get(k))));
                }
            } else if (j == this.paletteSize) {
                System.arraycopy(data, 0, this.data.getStorage(), 0, data.length);
            } else {
                PackedIntegerArray palette = new PackedIntegerArray(j, 4096, data);
                for (int packedIntegerArray = 0; packedIntegerArray < 4096; ++packedIntegerArray) {
                    this.data.set(packedIntegerArray, palette.get(packedIntegerArray));
                }
            }
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(NbtCompound nbt, String paletteKey, String dataKey) {
        try {
            this.lock();
            BiMapPalette<T> biMapPalette = new BiMapPalette<T>(this.idList, this.paletteSize, this.noOpPaletteResizeHandler, this.elementDeserializer, this.elementSerializer);
            T object = this.defaultValue;
            int i = biMapPalette.getIndex(this.defaultValue);
            int[] is = new int[4096];
            for (int j = 0; j < 4096; ++j) {
                T object2 = this.get(j);
                if (object2 != object) {
                    object = object2;
                    i = biMapPalette.getIndex(object2);
                }
                is[j] = i;
            }
            NbtList j = new NbtList();
            biMapPalette.writeNbt(j);
            nbt.put(paletteKey, j);
            int object2 = Math.max(4, MathHelper.log2DeBruijn(j.size()));
            PackedIntegerArray packedIntegerArray = new PackedIntegerArray(object2, 4096);
            for (int k = 0; k < is.length; ++k) {
                packedIntegerArray.set(k, is[k]);
            }
            nbt.putLongArray(dataKey, packedIntegerArray.getStorage());
        }
        finally {
            this.unlock();
        }
    }

    public int getPacketSize() {
        return 1 + this.palette.getPacketSize() + PacketByteBuf.getVarIntLength(this.data.getSize()) + this.data.getStorage().length * 8;
    }

    public boolean hasAny(Predicate<T> predicate) {
        return this.palette.accepts(predicate);
    }

    public void count(CountConsumer<T> consumer) {
        Int2IntOpenHashMap int2IntMap = new Int2IntOpenHashMap();
        this.data.forEach(i -> int2IntMap.put(i, int2IntMap.get(i) + 1));
        int2IntMap.int2IntEntrySet().forEach(entry -> consumer.accept(this.palette.getByIndex(entry.getIntKey()), entry.getIntValue()));
    }

    @FunctionalInterface
    public static interface CountConsumer<T> {
        public void accept(T var1, int var2);
    }
}

