/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.math.floatprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.floatprovider.FloatProviderType;

public class ConstantFloatProvider
extends FloatProvider {
    public static final ConstantFloatProvider ZERO = new ConstantFloatProvider(0.0f);
    public static final Codec<ConstantFloatProvider> CODEC = Codec.either(Codec.FLOAT, RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(constantFloatProvider -> Float.valueOf(constantFloatProvider.value))).apply((Applicative<ConstantFloatProvider, ?>)instance, ConstantFloatProvider::new))).xmap(either -> either.map(ConstantFloatProvider::create, constantFloatProvider -> constantFloatProvider), constantFloatProvider -> Either.left(Float.valueOf(constantFloatProvider.value)));
    private final float value;

    public static ConstantFloatProvider create(float value) {
        if (value == 0.0f) {
            return ZERO;
        }
        return new ConstantFloatProvider(value);
    }

    private ConstantFloatProvider(float value) {
        this.value = value;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public float get(Random random) {
        return this.value;
    }

    @Override
    public float getMin() {
        return this.value;
    }

    @Override
    public float getMax() {
        return this.value + 1.0f;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CONSTANT;
    }

    public String toString() {
        return Float.toString(this.value);
    }
}

