/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.nbt.visitor.NbtElementVisitor;

/**
 * Represents an NBT list.
 * <p>
 * An NBT list holds values of the same {@linkplain NbtElement#getType NBT type}.
 * The {@linkplain AbstractNbtList#getHeldType NBT type} of an NBT list is determined
 * once its first element is inserted; empty NBT lists return {@link NbtElement#NULL_TYPE NULL_TYPE} as their held {@linkplain AbstractNbtList#getHeldType NBT type}.
 */
public class NbtList
extends AbstractNbtList<NbtElement> {
    private static final int field_33199 = 296;
    public static final NbtType<NbtList> TYPE = new NbtType<NbtList>(){

        @Override
        public NbtList read(DataInput dataInput, int i, NbtTagSizeTracker nbtTagSizeTracker) throws IOException {
            nbtTagSizeTracker.add(296L);
            if (i > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            }
            byte b = dataInput.readByte();
            int j = dataInput.readInt();
            if (b == 0 && j > 0) {
                throw new RuntimeException("Missing type on ListTag");
            }
            nbtTagSizeTracker.add(32L * (long)j);
            NbtType<?> nbtType = NbtTypes.byId(b);
            ArrayList<NbtElement> list = Lists.newArrayListWithCapacity(j);
            for (int k = 0; k < j; ++k) {
                list.add((NbtElement)nbtType.read(dataInput, i + 1, nbtTagSizeTracker));
            }
            return new NbtList(list, b);
        }

        @Override
        public String getCrashReportName() {
            return "LIST";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_List";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
            return this.read(input, depth, tracker);
        }
    };
    private final List<NbtElement> value;
    private byte type;

    NbtList(List<NbtElement> list, byte type) {
        this.value = list;
        this.type = type;
    }

    public NbtList() {
        this(Lists.newArrayList(), 0);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        this.type = this.value.isEmpty() ? (byte)0 : this.value.get(0).getType();
        output.writeByte(this.type);
        output.writeInt(this.value.size());
        for (NbtElement nbtElement : this.value) {
            nbtElement.write(output);
        }
    }

    @Override
    public byte getType() {
        return 9;
    }

    public NbtType<NbtList> getNbtType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.asString();
    }

    private void forgetTypeIfEmpty() {
        if (this.value.isEmpty()) {
            this.type = 0;
        }
    }

    @Override
    public NbtElement remove(int i) {
        NbtElement nbtElement = this.value.remove(i);
        this.forgetTypeIfEmpty();
        return nbtElement;
    }

    @Override
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    public NbtCompound getCompound(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 10) {
            return (NbtCompound)nbtElement;
        }
        return new NbtCompound();
    }

    public NbtList getList(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 9) {
            return (NbtList)nbtElement;
        }
        return new NbtList();
    }

    public short getShort(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 2) {
            return ((NbtShort)nbtElement).shortValue();
        }
        return 0;
    }

    public int getInt(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 3) {
            return ((NbtInt)nbtElement).intValue();
        }
        return 0;
    }

    public int[] getIntArray(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 11) {
            return ((NbtIntArray)nbtElement).getIntArray();
        }
        return new int[0];
    }

    public long[] getLongArray(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 11) {
            return ((NbtLongArray)nbtElement).getLongArray();
        }
        return new long[0];
    }

    public double getDouble(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 6) {
            return ((NbtDouble)nbtElement).doubleValue();
        }
        return 0.0;
    }

    public float getFloat(int index) {
        NbtElement nbtElement;
        if (index >= 0 && index < this.value.size() && (nbtElement = this.value.get(index)).getType() == 5) {
            return ((NbtFloat)nbtElement).floatValue();
        }
        return 0.0f;
    }

    public String getString(int index) {
        if (index < 0 || index >= this.value.size()) {
            return "";
        }
        NbtElement nbtElement = this.value.get(index);
        if (nbtElement.getType() == 8) {
            return nbtElement.asString();
        }
        return nbtElement.toString();
    }

    @Override
    public int size() {
        return this.value.size();
    }

    @Override
    public NbtElement get(int i) {
        return this.value.get(i);
    }

    @Override
    public NbtElement set(int i, NbtElement nbtElement) {
        NbtElement nbtElement2 = this.get(i);
        if (!this.setElement(i, nbtElement)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", nbtElement.getType(), this.type));
        }
        return nbtElement2;
    }

    @Override
    public void add(int i, NbtElement nbtElement) {
        if (!this.addElement(i, nbtElement)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", nbtElement.getType(), this.type));
        }
    }

    @Override
    public boolean setElement(int index, NbtElement element) {
        if (this.canAdd(element)) {
            this.value.set(index, element);
            return true;
        }
        return false;
    }

    @Override
    public boolean addElement(int index, NbtElement element) {
        if (this.canAdd(element)) {
            this.value.add(index, element);
            return true;
        }
        return false;
    }

    private boolean canAdd(NbtElement element) {
        if (element.getType() == 0) {
            return false;
        }
        if (this.type == 0) {
            this.type = element.getType();
            return true;
        }
        return this.type == element.getType();
    }

    @Override
    public NbtList copy() {
        List<NbtElement> iterable = NbtTypes.byId(this.type).isImmutable() ? this.value : Iterables.transform(this.value, NbtElement::copy);
        ArrayList<NbtElement> list = Lists.newArrayList(iterable);
        return new NbtList(list, this.type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtList && Objects.equals(this.value, ((NbtList)o).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitList(this);
    }

    @Override
    public byte getHeldType() {
        return this.type;
    }

    @Override
    public void clear() {
        this.value.clear();
        this.type = 0;
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (NbtElement)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (NbtElement)object);
    }

    @Override
    public /* synthetic */ Object get(int index) {
        return this.get(index);
    }
}

