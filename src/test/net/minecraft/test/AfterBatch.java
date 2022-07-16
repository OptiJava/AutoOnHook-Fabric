/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code AfterBatch} methods are ran once the batch specified has finished.
 * 
 * <p>{@code AfterBatch} methods must take 1 parameter of {@link net.minecraft.server.world.ServerWorld}.
 */
@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface AfterBatch {
    public String batchId();
}

