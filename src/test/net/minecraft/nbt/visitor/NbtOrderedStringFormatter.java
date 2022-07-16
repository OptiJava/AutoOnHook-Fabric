/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.nbt.visitor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.Util;

/**
 * Formats an NBT element as a multiline string where named elements inside of compound objects
 * are sorted according to a defined ordering.
 */
public class NbtOrderedStringFormatter
implements NbtElementVisitor {
    /**
     * Contains the names of elements which should appear before any other element in a compound object, even
     * when they would otherwise appear later lexicographically. The list of elements which should be
     * prioritized differs depending on the path of the compound object.
     */
    private static final Map<String, List<String>> ENTRY_ORDER_OVERRIDES = Util.make(Maps.newHashMap(), map -> {
        map.put("{}", Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes"));
        map.put("{}.data.[].{}", Lists.newArrayList("pos", "state", "nbt"));
        map.put("{}.entities.[].{}", Lists.newArrayList("blockPos", "pos"));
    });
    /**
     * Contains paths for which the indentation prefix should not be prepended to the result.
     */
    private static final Set<String> IGNORED_PATHS = Sets.newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}");
    private static final Pattern SIMPLE_NAME = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String KEY_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ENTRY_SEPARATOR = String.valueOf(',');
    private static final String field_33234 = "[";
    private static final String field_33235 = "]";
    private static final String field_33236 = ";";
    private static final String field_33237 = " ";
    private static final String field_33238 = "{";
    private static final String field_33239 = "}";
    private static final String field_33240 = "\n";
    private final String prefix;
    private final int indentationLevel;
    private final List<String> pathParts;
    private String result;

    public NbtOrderedStringFormatter() {
        this("    ", 0, Lists.newArrayList());
    }

    public NbtOrderedStringFormatter(String prefix, int indentationLevel, List<String> pathParts) {
        this.prefix = prefix;
        this.indentationLevel = indentationLevel;
        this.pathParts = pathParts;
    }

    public String apply(NbtElement element) {
        element.accept(this);
        return this.result;
    }

    @Override
    public void visitString(NbtString element) {
        this.result = NbtString.escape(element.asString());
    }

    @Override
    public void visitByte(NbtByte element) {
        this.result = element.numberValue() + "b";
    }

    @Override
    public void visitShort(NbtShort element) {
        this.result = element.numberValue() + "s";
    }

    @Override
    public void visitInt(NbtInt element) {
        this.result = String.valueOf(element.numberValue());
    }

    @Override
    public void visitLong(NbtLong element) {
        this.result = element.numberValue() + "L";
    }

    @Override
    public void visitFloat(NbtFloat element) {
        this.result = element.floatValue() + "f";
    }

    @Override
    public void visitDouble(NbtDouble element) {
        this.result = element.doubleValue() + "d";
    }

    @Override
    public void visitByteArray(NbtByteArray element) {
        StringBuilder stringBuilder = new StringBuilder(field_33234).append("B").append(field_33236);
        byte[] bs = element.getByteArray();
        for (int i = 0; i < bs.length; ++i) {
            stringBuilder.append(field_33237).append(bs[i]).append("B");
            if (i == bs.length - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR);
        }
        stringBuilder.append(field_33235);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitIntArray(NbtIntArray element) {
        StringBuilder stringBuilder = new StringBuilder(field_33234).append("I").append(field_33236);
        int[] is = element.getIntArray();
        for (int i = 0; i < is.length; ++i) {
            stringBuilder.append(field_33237).append(is[i]);
            if (i == is.length - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR);
        }
        stringBuilder.append(field_33235);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitLongArray(NbtLongArray element) {
        String string = "L";
        StringBuilder stringBuilder = new StringBuilder(field_33234).append("L").append(field_33236);
        long[] ls = element.getLongArray();
        for (int i = 0; i < ls.length; ++i) {
            stringBuilder.append(field_33237).append(ls[i]).append("L");
            if (i == ls.length - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR);
        }
        stringBuilder.append(field_33235);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitList(NbtList element) {
        String string;
        if (element.isEmpty()) {
            this.result = "[]";
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(field_33234);
        this.pushPathPart("[]");
        String string2 = string = IGNORED_PATHS.contains(this.joinPath()) ? "" : this.prefix;
        if (!string.isEmpty()) {
            stringBuilder.append(field_33240);
        }
        for (int i = 0; i < element.size(); ++i) {
            stringBuilder.append(Strings.repeat(string, this.indentationLevel + 1));
            stringBuilder.append(new NbtOrderedStringFormatter(string, this.indentationLevel + 1, this.pathParts).apply(element.get(i)));
            if (i == element.size() - 1) continue;
            stringBuilder.append(ENTRY_SEPARATOR).append(string.isEmpty() ? field_33237 : field_33240);
        }
        if (!string.isEmpty()) {
            stringBuilder.append(field_33240).append(Strings.repeat(string, this.indentationLevel));
        }
        stringBuilder.append(field_33235);
        this.result = stringBuilder.toString();
        this.popPathPart();
    }

    @Override
    public void visitCompound(NbtCompound compound) {
        String string;
        if (compound.isEmpty()) {
            this.result = "{}";
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(field_33238);
        this.pushPathPart("{}");
        String string2 = string = IGNORED_PATHS.contains(this.joinPath()) ? "" : this.prefix;
        if (!string.isEmpty()) {
            stringBuilder.append(field_33240);
        }
        List<String> collection = this.getSortedNames(compound);
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            String string22 = (String)iterator.next();
            NbtElement nbtElement = compound.get(string22);
            this.pushPathPart(string22);
            stringBuilder.append(Strings.repeat(string, this.indentationLevel + 1)).append(NbtOrderedStringFormatter.escapeName(string22)).append(KEY_VALUE_SEPARATOR).append(field_33237).append(new NbtOrderedStringFormatter(string, this.indentationLevel + 1, this.pathParts).apply(nbtElement));
            this.popPathPart();
            if (!iterator.hasNext()) continue;
            stringBuilder.append(ENTRY_SEPARATOR).append(string.isEmpty() ? field_33237 : field_33240);
        }
        if (!string.isEmpty()) {
            stringBuilder.append(field_33240).append(Strings.repeat(string, this.indentationLevel));
        }
        stringBuilder.append(field_33239);
        this.result = stringBuilder.toString();
        this.popPathPart();
    }

    private void popPathPart() {
        this.pathParts.remove(this.pathParts.size() - 1);
    }

    private void pushPathPart(String part) {
        this.pathParts.add(part);
    }

    protected List<String> getSortedNames(NbtCompound compound) {
        HashSet<String> set = Sets.newHashSet(compound.getKeys());
        ArrayList<String> list = Lists.newArrayList();
        List<String> list2 = ENTRY_ORDER_OVERRIDES.get(this.joinPath());
        if (list2 != null) {
            for (String string : list2) {
                if (!set.remove(string)) continue;
                list.add(string);
            }
            if (!set.isEmpty()) {
                set.stream().sorted().forEach(list::add);
            }
        } else {
            list.addAll(set);
            Collections.sort(list);
        }
        return list;
    }

    public String joinPath() {
        return String.join((CharSequence)".", this.pathParts);
    }

    protected static String escapeName(String name) {
        if (SIMPLE_NAME.matcher(name).matches()) {
            return name;
        }
        return NbtString.escape(name);
    }

    @Override
    public void visitNull(NbtNull element) {
    }
}

