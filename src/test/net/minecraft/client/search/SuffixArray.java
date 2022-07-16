/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides an efficient way to search for a text in multiple texts.
 */
@Environment(value=EnvType.CLIENT)
public class SuffixArray<T> {
    private static final boolean PRINT_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
    private static final boolean PRINT_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int field_33013 = -1;
    private static final int field_33014 = -2;
    protected final List<T> objects = Lists.newArrayList();
    private final IntList characters = new IntArrayList();
    private final IntList textStarts = new IntArrayList();
    private IntList suffixIndexToObjectIndex = new IntArrayList();
    private IntList offsetInText = new IntArrayList();
    private int maxTextLength;

    /**
     * Adds a text with the corresponding object.
     * 
     * <p>You are not allowed to call this method after calling {@link #build()} method.
     * 
     * <p>Takes O({@code text.length()}) time.
     */
    public void add(T object, String text) {
        this.maxTextLength = Math.max(this.maxTextLength, text.length());
        int i = this.objects.size();
        this.objects.add(object);
        this.textStarts.add(this.characters.size());
        for (int j = 0; j < text.length(); ++j) {
            this.suffixIndexToObjectIndex.add(i);
            this.offsetInText.add(j);
            this.characters.add(text.charAt(j));
        }
        this.suffixIndexToObjectIndex.add(i);
        this.offsetInText.add(text.length());
        this.characters.add(-1);
    }

    /**
     * Builds a suffix array with added texts.
     * 
     * <p>You are not allowed to call this method multiple times.
     * 
     * <p>Takes O(N * log N * log M) time on average where N is the sum of all text
     * length added, and M is the maximum text length added.
     */
    public void build() {
        int j2;
        int i2 = this.characters.size();
        int[] is = new int[i2];
        final int[] js = new int[i2];
        final int[] ks = new int[i2];
        int[] ls = new int[i2];
        IntComparator intComparator = new IntComparator(){

            @Override
            public int compare(int i, int j) {
                if (js[i] == js[j]) {
                    return Integer.compare(ks[i], ks[j]);
                }
                return Integer.compare(js[i], js[j]);
            }

            @Override
            public int compare(Integer integer, Integer integer2) {
                return this.compare((int)integer, (int)integer2);
            }
        };
        Swapper swapper = (i, j) -> {
            if (i != j) {
                int k = js[i];
                is[i] = js[j];
                is[j] = k;
                k = ks[i];
                js[i] = ks[j];
                js[j] = k;
                k = ls[i];
                ks[i] = ls[j];
                ks[j] = k;
            }
        };
        for (j2 = 0; j2 < i2; ++j2) {
            is[j2] = this.characters.getInt(j2);
        }
        j2 = 1;
        int k = Math.min(i2, this.maxTextLength);
        while (j2 * 2 < k) {
            int l;
            for (l = 0; l < i2; ++l) {
                js[l] = is[l];
                ks[l] = l + j2 < i2 ? is[l + j2] : -2;
                ls[l] = l;
            }
            it.unimi.dsi.fastutil.Arrays.quickSort(0, i2, intComparator, swapper);
            for (l = 0; l < i2; ++l) {
                is[ls[l]] = l > 0 && js[l] == js[l - 1] && ks[l] == ks[l - 1] ? is[ls[l - 1]] : l;
            }
            j2 *= 2;
        }
        IntList l = this.suffixIndexToObjectIndex;
        IntList intList = this.offsetInText;
        this.suffixIndexToObjectIndex = new IntArrayList(l.size());
        this.offsetInText = new IntArrayList(intList.size());
        for (int m = 0; m < i2; ++m) {
            int n = ls[m];
            this.suffixIndexToObjectIndex.add(l.getInt(n));
            this.offsetInText.add(intList.getInt(n));
        }
        if (PRINT_ARRAY) {
            this.printArray();
        }
    }

    private void printArray() {
        for (int i = 0; i < this.suffixIndexToObjectIndex.size(); ++i) {
            LOGGER.debug("{} {}", (Object)i, (Object)this.getDebugString(i));
        }
        LOGGER.debug("");
    }

    private String getDebugString(int suffixIndex) {
        int i = this.offsetInText.getInt(suffixIndex);
        int j = this.textStarts.getInt(this.suffixIndexToObjectIndex.getInt(suffixIndex));
        StringBuilder stringBuilder = new StringBuilder();
        int k = 0;
        while (j + k < this.characters.size()) {
            int l;
            if (k == i) {
                stringBuilder.append('^');
            }
            if ((l = this.characters.get(j + k).intValue()) == -1) break;
            stringBuilder.append((char)l);
            ++k;
        }
        return stringBuilder.toString();
    }

    private int compare(String string, int suffixIndex) {
        int i = this.textStarts.getInt(this.suffixIndexToObjectIndex.getInt(suffixIndex));
        int j = this.offsetInText.getInt(suffixIndex);
        for (int k = 0; k < string.length(); ++k) {
            char d;
            int l = this.characters.getInt(i + j + k);
            if (l == -1) {
                return 1;
            }
            char c = string.charAt(k);
            if (c < (d = (char)l)) {
                return -1;
            }
            if (c <= d) continue;
            return 1;
        }
        return 0;
    }

    /**
     * Retrieves all objects of which corresponding texts contain {@code text}.
     * 
     * <p>You have to call {@link #build()} method before calling this method.
     * 
     * <p>Takes O({@code text.length()} * log N) time to find objects where N is the
     * sum of all text length added. Takes O(X + Y * log Y) time to collect found
     * objects into a list where X is the number of occurrences of {@code text} in all
     * texts added, and Y is the number of found objects.
     */
    public List<T> findAll(String text) {
        int m;
        int l;
        int i = this.suffixIndexToObjectIndex.size();
        int j = 0;
        int k = i;
        while (j < k) {
            l = j + (k - j) / 2;
            m = this.compare(text, l);
            if (PRINT_COMPARISONS) {
                LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", (Object)text, (Object)l, (Object)this.getDebugString(l), (Object)m);
            }
            if (m > 0) {
                j = l + 1;
                continue;
            }
            k = l;
        }
        if (j < 0 || j >= i) {
            return Collections.emptyList();
        }
        l = j;
        k = i;
        while (j < k) {
            m = j + (k - j) / 2;
            int n = this.compare(text, m);
            if (PRINT_COMPARISONS) {
                LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", (Object)text, (Object)m, (Object)this.getDebugString(m), (Object)n);
            }
            if (n >= 0) {
                j = m + 1;
                continue;
            }
            k = m;
        }
        m = j;
        IntOpenHashSet n = new IntOpenHashSet();
        for (int o = l; o < m; ++o) {
            n.add(this.suffixIndexToObjectIndex.getInt(o));
        }
        int[] o = n.toIntArray();
        Arrays.sort(o);
        LinkedHashSet<T> set = Sets.newLinkedHashSet();
        for (int p : o) {
            set.add(this.objects.get(p));
        }
        return Lists.newArrayList(set);
    }
}

