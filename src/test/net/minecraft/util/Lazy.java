/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

@Deprecated
public class Lazy<T> {
    private final Supplier<T> supplier = Suppliers.memoize(delegate::get);

    public Lazy(Supplier<T> delegate) {
    }

    public T get() {
        return this.supplier.get();
    }
}

