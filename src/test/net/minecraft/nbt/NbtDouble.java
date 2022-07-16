/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.math.MathHelper;

/**
 * Represents an NBT 64-bit floating-point number.
 */
public class NbtDouble
extends AbstractNbtNumber {
    private static final int field_33192 = 128;
    public static final NbtDouble ZERO = new NbtDouble(0.0);
    public static final NbtType<NbtDouble> TYPE = new NbtType<NbtDouble>(){

        @Override
        public NbtDouble read(DataInput dataInput, int i, NbtTagSizeTracker nbtTagSizeTracker) throws IOException {
            nbtTagSizeTracker.add(128L);
            return NbtDouble.of(dataInput.readDouble());
        }

        @Override
        public String getCrashReportName() {
            return "DOUBLE";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Double";
        }

        @Override
        public boolean isImmutable() {
            return true;
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
            return this.read(input, depth, tracker);
        }
    };
    private final double value;

    private NbtDouble(double value) {
        this.value = value;
    }

    public static NbtDouble of(double value) {
        if (value == 0.0) {
            return ZERO;
        }
        return new NbtDouble(value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeDouble(this.value);
    }

    @Override
    public byte getType() {
        return 6;
    }

    public NbtType<NbtDouble> getNbtType() {
        return TYPE;
    }

    @Override
    public NbtDouble copy() {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtDouble && this.value == ((NbtDouble)o).value;
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(this.value);
        return (int)(l ^ l >>> 32);
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitDouble(this);
    }

    @Override
    public long longValue() {
        return (long)Math.floor(this.value);
    }

    @Override
    public int intValue() {
        return MathHelper.floor(this.value);
    }

    @Override
    public short shortValue() {
        return (short)(MathHelper.floor(this.value) & 0xFFFF);
    }

    @Override
    public byte byteValue() {
        return (byte)(MathHelper.floor(this.value) & 0xFF);
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return (float)this.value;
    }

    @Override
    public Number numberValue() {
        return this.value;
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }
}

