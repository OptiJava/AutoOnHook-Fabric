/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.dynamic;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

/**
 * A codec for {@link SimpleRegistry}.
 * 
 * <p>Compared to regular codec, this codec performs additional work when
 * decoding, loading its elements from the given resource manager's JSON
 * files.
 * 
 * @param <E> the registry's element type
 * @see RegistryElementCodec
 * @see RegistryOps
 */
public final class RegistryCodec<E>
implements Codec<SimpleRegistry<E>> {
    private final Codec<SimpleRegistry<E>> delegate;
    private final RegistryKey<? extends Registry<E>> registryRef;
    private final Codec<E> elementCodec;

    public static <E> RegistryCodec<E> of(RegistryKey<? extends Registry<E>> registryRef, Lifecycle lifecycle, Codec<E> codec) {
        return new RegistryCodec<E>(registryRef, lifecycle, codec);
    }

    private RegistryCodec(RegistryKey<? extends Registry<E>> registryRef, Lifecycle lifecycle, Codec<E> codec) {
        this.delegate = SimpleRegistry.createCodec(registryRef, lifecycle, codec);
        this.registryRef = registryRef;
        this.elementCodec = codec;
    }

    @Override
    public <T> DataResult<T> encode(SimpleRegistry<E> simpleRegistry, DynamicOps<T> dynamicOps, T object) {
        return this.delegate.encode(simpleRegistry, dynamicOps, object);
    }

    @Override
    public <T> DataResult<Pair<SimpleRegistry<E>, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<Pair<SimpleRegistry<E>, T>> dataResult = this.delegate.decode(ops, input);
        if (ops instanceof RegistryOps) {
            return dataResult.flatMap((? super R pair) -> ((RegistryOps)ops).loadToRegistry((SimpleRegistry)pair.getFirst(), this.registryRef, this.elementCodec).map((? super R simpleRegistry) -> Pair.of(simpleRegistry, pair.getSecond())));
        }
        return dataResult;
    }

    public String toString() {
        return "RegistryDataPackCodec[" + this.delegate + " " + this.registryRef + " " + this.elementCodec + "]";
    }

    @Override
    public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
        return this.encode((SimpleRegistry)input, ops, prefix);
    }
}

