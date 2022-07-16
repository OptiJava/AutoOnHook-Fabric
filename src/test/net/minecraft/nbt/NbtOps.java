/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtNull;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

/**
 * Used to handle Minecraft NBTs within {@link com.mojang.serialization.Dynamic
 * dynamics} for DataFixerUpper, allowing generalized serialization logic
 * shared across different type of data structures. Use {@link NbtOps#INSTANCE}
 * for the ops singleton.
 * 
 * <p>For instance, dimension data may be stored as JSON in data packs, but
 * they will be transported in packets as NBT. DataFixerUpper allows
 * generalizing the dimension serialization logic to prevent duplicate code,
 * where the NBT ops allow the DataFixerUpper dimension serialization logic
 * to interact with Minecraft NBTs.
 * 
 * @see NbtOps#INSTANCE
 */
public class NbtOps
implements DynamicOps<NbtElement> {
    /**
     * An singleton of the NBT dynamic ops.
     * 
     * <p>This ops does not compress maps (replace field name to value pairs
     * with an ordered list of values in serialization). In fact, since
     * Minecraft NBT lists can only contain elements of the same type, this op
     * cannot compress maps.
     */
    public static final NbtOps INSTANCE = new NbtOps();

    protected NbtOps() {
    }

    @Override
    public NbtElement empty() {
        return NbtNull.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> dynamicOps, NbtElement nbtElement) {
        switch (nbtElement.getType()) {
            case 0: {
                return dynamicOps.empty();
            }
            case 1: {
                return dynamicOps.createByte(((AbstractNbtNumber)nbtElement).byteValue());
            }
            case 2: {
                return dynamicOps.createShort(((AbstractNbtNumber)nbtElement).shortValue());
            }
            case 3: {
                return dynamicOps.createInt(((AbstractNbtNumber)nbtElement).intValue());
            }
            case 4: {
                return dynamicOps.createLong(((AbstractNbtNumber)nbtElement).longValue());
            }
            case 5: {
                return dynamicOps.createFloat(((AbstractNbtNumber)nbtElement).floatValue());
            }
            case 6: {
                return dynamicOps.createDouble(((AbstractNbtNumber)nbtElement).doubleValue());
            }
            case 7: {
                return dynamicOps.createByteList(ByteBuffer.wrap(((NbtByteArray)nbtElement).getByteArray()));
            }
            case 8: {
                return dynamicOps.createString(nbtElement.asString());
            }
            case 9: {
                return this.convertList(dynamicOps, nbtElement);
            }
            case 10: {
                return this.convertMap(dynamicOps, nbtElement);
            }
            case 11: {
                return dynamicOps.createIntList(Arrays.stream(((NbtIntArray)nbtElement).getIntArray()));
            }
            case 12: {
                return dynamicOps.createLongList(Arrays.stream(((NbtLongArray)nbtElement).getLongArray()));
            }
        }
        throw new IllegalStateException("Unknown tag type: " + nbtElement);
    }

    @Override
    public DataResult<Number> getNumberValue(NbtElement nbtElement) {
        if (nbtElement instanceof AbstractNbtNumber) {
            return DataResult.success(((AbstractNbtNumber)nbtElement).numberValue());
        }
        return DataResult.error("Not a number");
    }

    @Override
    public NbtElement createNumeric(Number number) {
        return NbtDouble.of(number.doubleValue());
    }

    @Override
    public NbtElement createByte(byte b) {
        return NbtByte.of(b);
    }

    @Override
    public NbtElement createShort(short s) {
        return NbtShort.of(s);
    }

    @Override
    public NbtElement createInt(int i) {
        return NbtInt.of(i);
    }

    @Override
    public NbtElement createLong(long l) {
        return NbtLong.of(l);
    }

    @Override
    public NbtElement createFloat(float f) {
        return NbtFloat.of(f);
    }

    @Override
    public NbtElement createDouble(double d) {
        return NbtDouble.of(d);
    }

    @Override
    public NbtElement createBoolean(boolean bl) {
        return NbtByte.of(bl);
    }

    @Override
    public DataResult<String> getStringValue(NbtElement nbtElement) {
        if (nbtElement instanceof NbtString) {
            return DataResult.success(nbtElement.asString());
        }
        return DataResult.error("Not a string");
    }

    @Override
    public NbtElement createString(String string) {
        return NbtString.of(string);
    }

    private static AbstractNbtList<?> method_29144(byte b, byte c) {
        if (NbtOps.method_29145(b, c, (byte)4)) {
            return new NbtLongArray(new long[0]);
        }
        if (NbtOps.method_29145(b, c, (byte)1)) {
            return new NbtByteArray(new byte[0]);
        }
        if (NbtOps.method_29145(b, c, (byte)3)) {
            return new NbtIntArray(new int[0]);
        }
        return new NbtList();
    }

    private static boolean method_29145(byte b, byte c, byte d) {
        return b == d && (c == d || c == 0);
    }

    private static <T extends NbtElement> void method_29151(AbstractNbtList<T> abstractNbtList, NbtElement nbtElement2, NbtElement nbtElement22) {
        if (nbtElement2 instanceof AbstractNbtList) {
            AbstractNbtList abstractNbtList2 = (AbstractNbtList)nbtElement2;
            abstractNbtList2.forEach(nbtElement -> abstractNbtList.add(nbtElement));
        }
        abstractNbtList.add(nbtElement22);
    }

    private static <T extends NbtElement> void method_29150(AbstractNbtList<T> abstractNbtList, NbtElement nbtElement2, List<NbtElement> list) {
        if (nbtElement2 instanceof AbstractNbtList) {
            AbstractNbtList abstractNbtList2 = (AbstractNbtList)nbtElement2;
            abstractNbtList2.forEach(nbtElement -> abstractNbtList.add(nbtElement));
        }
        list.forEach(nbtElement -> abstractNbtList.add(nbtElement));
    }

    @Override
    public DataResult<NbtElement> mergeToList(NbtElement nbtElement, NbtElement nbtElement2) {
        if (!(nbtElement instanceof AbstractNbtList) && !(nbtElement instanceof NbtNull)) {
            return DataResult.error("mergeToList called with not a list: " + nbtElement, nbtElement);
        }
        AbstractNbtList<?> abstractNbtList = NbtOps.method_29144(nbtElement instanceof AbstractNbtList ? ((AbstractNbtList)nbtElement).getHeldType() : (byte)0, nbtElement2.getType());
        NbtOps.method_29151(abstractNbtList, nbtElement, nbtElement2);
        return DataResult.success(abstractNbtList);
    }

    @Override
    public DataResult<NbtElement> mergeToList(NbtElement nbtElement, List<NbtElement> list) {
        if (!(nbtElement instanceof AbstractNbtList) && !(nbtElement instanceof NbtNull)) {
            return DataResult.error("mergeToList called with not a list: " + nbtElement, nbtElement);
        }
        AbstractNbtList<?> abstractNbtList = NbtOps.method_29144(nbtElement instanceof AbstractNbtList ? ((AbstractNbtList)nbtElement).getHeldType() : (byte)0, list.stream().findFirst().map(NbtElement::getType).orElse((byte)0));
        NbtOps.method_29150(abstractNbtList, nbtElement, list);
        return DataResult.success(abstractNbtList);
    }

    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, NbtElement nbtElement2, NbtElement nbtElement3) {
        if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtNull)) {
            return DataResult.error("mergeToMap called with not a map: " + nbtElement, nbtElement);
        }
        if (!(nbtElement2 instanceof NbtString)) {
            return DataResult.error("key is not a string: " + nbtElement2, nbtElement);
        }
        NbtCompound nbtCompound = new NbtCompound();
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
            nbtCompound2.getKeys().forEach(string -> nbtCompound.put((String)string, nbtCompound2.get((String)string)));
        }
        nbtCompound.put(nbtElement2.asString(), nbtElement3);
        return DataResult.success(nbtCompound);
    }

    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, MapLike<NbtElement> mapLike) {
        Object nbtCompound2;
        if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtNull)) {
            return DataResult.error("mergeToMap called with not a map: " + nbtElement, nbtElement);
        }
        NbtCompound nbtCompound = new NbtCompound();
        if (nbtElement instanceof NbtCompound) {
            nbtCompound2 = (NbtCompound)nbtElement;
            ((NbtCompound)nbtCompound2).getKeys().forEach(arg_0 -> NbtOps.method_29159(nbtCompound, (NbtCompound)nbtCompound2, arg_0));
        }
        nbtCompound2 = Lists.newArrayList();
        mapLike.entries().forEach(arg_0 -> NbtOps.method_29147((List)nbtCompound2, nbtCompound, arg_0));
        if (!nbtCompound2.isEmpty()) {
            return DataResult.error("some keys are not strings: " + (List)nbtCompound2, nbtCompound);
        }
        return DataResult.success(nbtCompound);
    }

    @Override
    public DataResult<Stream<Pair<NbtElement, NbtElement>>> getMapValues(NbtElement nbtElement) {
        if (!(nbtElement instanceof NbtCompound)) {
            return DataResult.error("Not a map: " + nbtElement);
        }
        NbtCompound nbtCompound = (NbtCompound)nbtElement;
        return DataResult.success(nbtCompound.getKeys().stream().map(string -> Pair.of(this.createString((String)string), nbtCompound.get((String)string))));
    }

    @Override
    public DataResult<Consumer<BiConsumer<NbtElement, NbtElement>>> getMapEntries(NbtElement nbtElement) {
        if (!(nbtElement instanceof NbtCompound)) {
            return DataResult.error("Not a map: " + nbtElement);
        }
        NbtCompound nbtCompound = (NbtCompound)nbtElement;
        return DataResult.success(biConsumer -> nbtCompound.getKeys().forEach(string -> biConsumer.accept(this.createString((String)string), nbtCompound.get((String)string))));
    }

    @Override
    public DataResult<MapLike<NbtElement>> getMap(NbtElement nbtElement) {
        if (!(nbtElement instanceof NbtCompound)) {
            return DataResult.error("Not a map: " + nbtElement);
        }
        final NbtCompound nbtCompound = (NbtCompound)nbtElement;
        return DataResult.success(new MapLike<NbtElement>(){

            @Override
            @Nullable
            public NbtElement get(NbtElement nbtElement) {
                return nbtCompound.get(nbtElement.asString());
            }

            @Override
            @Nullable
            public NbtElement get(String string) {
                return nbtCompound.get(string);
            }

            @Override
            public Stream<Pair<NbtElement, NbtElement>> entries() {
                return nbtCompound.getKeys().stream().map(string -> Pair.of(NbtOps.this.createString((String)string), nbtCompound.get((String)string)));
            }

            public String toString() {
                return "MapLike[" + nbtCompound + "]";
            }

            @Override
            @Nullable
            public /* synthetic */ Object get(String string) {
                return this.get(string);
            }

            @Override
            @Nullable
            public /* synthetic */ Object get(Object object) {
                return this.get((NbtElement)object);
            }
        });
    }

    @Override
    public NbtElement createMap(Stream<Pair<NbtElement, NbtElement>> stream) {
        NbtCompound nbtCompound = new NbtCompound();
        stream.forEach(pair -> nbtCompound.put(((NbtElement)pair.getFirst()).asString(), (NbtElement)pair.getSecond()));
        return nbtCompound;
    }

    @Override
    public DataResult<Stream<NbtElement>> getStream(NbtElement nbtElement2) {
        if (nbtElement2 instanceof AbstractNbtList) {
            return DataResult.success(((AbstractNbtList)nbtElement2).stream().map(nbtElement -> nbtElement));
        }
        return DataResult.error("Not a list");
    }

    @Override
    public DataResult<Consumer<Consumer<NbtElement>>> getList(NbtElement nbtElement) {
        if (nbtElement instanceof AbstractNbtList) {
            AbstractNbtList abstractNbtList = (AbstractNbtList)nbtElement;
            return DataResult.success(abstractNbtList::forEach);
        }
        return DataResult.error("Not a list: " + nbtElement);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(NbtElement nbtElement) {
        if (nbtElement instanceof NbtByteArray) {
            return DataResult.success(ByteBuffer.wrap(((NbtByteArray)nbtElement).getByteArray()));
        }
        return DynamicOps.super.getByteBuffer(nbtElement);
    }

    @Override
    public NbtElement createByteList(ByteBuffer byteBuffer) {
        return new NbtByteArray(DataFixUtils.toArray(byteBuffer));
    }

    @Override
    public DataResult<IntStream> getIntStream(NbtElement nbtElement) {
        if (nbtElement instanceof NbtIntArray) {
            return DataResult.success(Arrays.stream(((NbtIntArray)nbtElement).getIntArray()));
        }
        return DynamicOps.super.getIntStream(nbtElement);
    }

    @Override
    public NbtElement createIntList(IntStream intStream) {
        return new NbtIntArray(intStream.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(NbtElement nbtElement) {
        if (nbtElement instanceof NbtLongArray) {
            return DataResult.success(Arrays.stream(((NbtLongArray)nbtElement).getLongArray()));
        }
        return DynamicOps.super.getLongStream(nbtElement);
    }

    @Override
    public NbtElement createLongList(LongStream longStream) {
        return new NbtLongArray(longStream.toArray());
    }

    @Override
    public NbtElement createList(Stream<NbtElement> stream) {
        PeekingIterator peekingIterator = Iterators.peekingIterator(stream.iterator());
        if (!peekingIterator.hasNext()) {
            return new NbtList();
        }
        NbtElement nbtElement2 = (NbtElement)peekingIterator.peek();
        if (nbtElement2 instanceof NbtByte) {
            ArrayList<Byte> list = Lists.newArrayList(Iterators.transform(peekingIterator, nbtElement -> ((NbtByte)nbtElement).byteValue()));
            return new NbtByteArray(list);
        }
        if (nbtElement2 instanceof NbtInt) {
            ArrayList<Integer> list = Lists.newArrayList(Iterators.transform(peekingIterator, nbtElement -> ((NbtInt)nbtElement).intValue()));
            return new NbtIntArray(list);
        }
        if (nbtElement2 instanceof NbtLong) {
            ArrayList<Long> list = Lists.newArrayList(Iterators.transform(peekingIterator, nbtElement -> ((NbtLong)nbtElement).longValue()));
            return new NbtLongArray(list);
        }
        NbtList list = new NbtList();
        while (peekingIterator.hasNext()) {
            NbtElement nbtElement22 = (NbtElement)peekingIterator.next();
            if (nbtElement22 instanceof NbtNull) continue;
            list.add(nbtElement22);
        }
        return list;
    }

    @Override
    public NbtElement remove(NbtElement nbtElement, String string) {
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)nbtElement;
            NbtCompound nbtCompound2 = new NbtCompound();
            nbtCompound.getKeys().stream().filter(k -> !Objects.equals(k, string)).forEach(k -> nbtCompound2.put((String)k, nbtCompound.get((String)k)));
            return nbtCompound2;
        }
        return nbtElement;
    }

    public String toString() {
        return "NBT";
    }

    @Override
    public RecordBuilder<NbtElement> mapBuilder() {
        return new MapBuilder();
    }

    @Override
    public /* synthetic */ Object remove(Object element, String key) {
        return this.remove((NbtElement)element, key);
    }

    @Override
    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    @Override
    public /* synthetic */ DataResult getLongStream(Object element) {
        return this.getLongStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    @Override
    public /* synthetic */ DataResult getIntStream(Object element) {
        return this.getIntStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    @Override
    public /* synthetic */ DataResult getByteBuffer(Object element) {
        return this.getByteBuffer((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    @Override
    public /* synthetic */ DataResult getList(Object element) {
        return this.getList((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getStream(Object element) {
        return this.getStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getMap(Object element) {
        return this.getMap((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap(stream);
    }

    @Override
    public /* synthetic */ DataResult getMapEntries(Object element) {
        return this.getMapEntries((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getMapValues(Object element) {
        return this.getMapValues((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object element, MapLike mapLike) {
        return this.mergeToMap((NbtElement)element, (MapLike<NbtElement>)mapLike);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object map, Object key, Object value) {
        return this.mergeToMap((NbtElement)map, (NbtElement)key, (NbtElement)value);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((NbtElement)object, (List<NbtElement>)list);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object list, Object value) {
        return this.mergeToList((NbtElement)list, (NbtElement)value);
    }

    @Override
    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    @Override
    public /* synthetic */ DataResult getStringValue(Object element) {
        return this.getStringValue((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createBoolean(boolean value) {
        return this.createBoolean(value);
    }

    @Override
    public /* synthetic */ Object createDouble(double value) {
        return this.createDouble(value);
    }

    @Override
    public /* synthetic */ Object createFloat(float value) {
        return this.createFloat(value);
    }

    @Override
    public /* synthetic */ Object createLong(long value) {
        return this.createLong(value);
    }

    @Override
    public /* synthetic */ Object createInt(int value) {
        return this.createInt(value);
    }

    @Override
    public /* synthetic */ Object createShort(short value) {
        return this.createShort(value);
    }

    @Override
    public /* synthetic */ Object createByte(byte value) {
        return this.createByte(value);
    }

    @Override
    public /* synthetic */ Object createNumeric(Number value) {
        return this.createNumeric(value);
    }

    @Override
    public /* synthetic */ DataResult getNumberValue(Object element) {
        return this.getNumberValue((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object element) {
        return this.convertTo(dynamicOps, (NbtElement)element);
    }

    @Override
    public /* synthetic */ Object empty() {
        return this.empty();
    }

    private static /* synthetic */ void method_29147(List list, NbtCompound nbtCompound, Pair pair) {
        NbtElement nbtElement = (NbtElement)pair.getFirst();
        if (!(nbtElement instanceof NbtString)) {
            list.add(nbtElement);
            return;
        }
        nbtCompound.put(nbtElement.asString(), (NbtElement)pair.getSecond());
    }

    private static /* synthetic */ void method_29159(NbtCompound nbtCompound, NbtCompound nbtCompound2, String string) {
        nbtCompound.put(string, nbtCompound2.get(string));
    }

    class MapBuilder
    extends RecordBuilder.AbstractStringBuilder<NbtElement, NbtCompound> {
        protected MapBuilder() {
            super(NbtOps.this);
        }

        @Override
        protected NbtCompound initBuilder() {
            return new NbtCompound();
        }

        @Override
        protected NbtCompound append(String string, NbtElement nbtElement, NbtCompound nbtCompound) {
            nbtCompound.put(string, nbtElement);
            return nbtCompound;
        }

        @Override
        protected DataResult<NbtElement> build(NbtCompound nbtCompound, NbtElement nbtElement) {
            if (nbtElement == null || nbtElement == NbtNull.INSTANCE) {
                return DataResult.success(nbtCompound);
            }
            if (nbtElement instanceof NbtCompound) {
                NbtCompound nbtCompound2 = new NbtCompound(Maps.newHashMap(((NbtCompound)nbtElement).toMap()));
                for (Map.Entry<String, NbtElement> entry : nbtCompound.toMap().entrySet()) {
                    nbtCompound2.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success(nbtCompound2);
            }
            return DataResult.error("mergeToMap called with not a map: " + nbtElement, nbtElement);
        }

        @Override
        protected /* synthetic */ Object append(String string, Object object, Object object2) {
            return this.append(string, (NbtElement)object, (NbtCompound)object2);
        }

        @Override
        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((NbtCompound)object, (NbtElement)object2);
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

