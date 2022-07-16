/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.math.intprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;

public class BiasedToBottomIntProvider
extends IntProvider {
    public static final Codec<BiasedToBottomIntProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("min_inclusive")).forGetter(biasedToBottomIntProvider -> biasedToBottomIntProvider.min), ((MapCodec)Codec.INT.fieldOf("max_inclusive")).forGetter(biasedToBottomIntProvider -> biasedToBottomIntProvider.max)).apply((Applicative<BiasedToBottomIntProvider, ?>)instance, BiasedToBottomIntProvider::new)).comapFlatMap(biasedToBottomIntProvider -> {
        if (biasedToBottomIntProvider.max < biasedToBottomIntProvider.min) {
            return DataResult.error("Max must be at least min, min_inclusive: " + biasedToBottomIntProvider.min + ", max_inclusive: " + biasedToBottomIntProvider.max);
        }
        return DataResult.success(biasedToBottomIntProvider);
    }, Function.identity());
    private final int min;
    private final int max;

    private BiasedToBottomIntProvider(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * @param min the minimum value, inclusive
     * @param max the maximum value, inclusive
     */
    public static BiasedToBottomIntProvider create(int min, int max) {
        return new BiasedToBottomIntProvider(min, max);
    }

    @Override
    public int get(Random random) {
        return this.min + random.nextInt(random.nextInt(this.max - this.min + 1) + 1);
    }

    @Override
    public int getMin() {
        return this.min;
    }

    @Override
    public int getMax() {
        return this.max;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.BIASED_TO_BOTTOM;
    }

    public String toString() {
        return "[" + this.min + "-" + this.max + "]";
    }
}

