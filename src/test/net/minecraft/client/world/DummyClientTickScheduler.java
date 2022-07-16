/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickPriority;
import net.minecraft.world.TickScheduler;

public class DummyClientTickScheduler<T>
implements TickScheduler<T> {
    private static final DummyClientTickScheduler<Object> INSTANCE = new DummyClientTickScheduler();

    public static <T> DummyClientTickScheduler<T> get() {
        return INSTANCE;
    }

    @Override
    public boolean isScheduled(BlockPos pos, T object) {
        return false;
    }

    @Override
    public void schedule(BlockPos pos, T object, int delay) {
    }

    @Override
    public void schedule(BlockPos pos, T object, int delay, TickPriority priority) {
    }

    @Override
    public boolean isTicking(BlockPos pos, T object) {
        return false;
    }

    @Override
    public int getTicks() {
        return 0;
    }
}

