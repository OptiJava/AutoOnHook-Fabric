/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A fetch rate limiter allows tracking the period of realms data fetcher
 * tasks even when the fetcher has stopped running. It prevents spamming
 * rate-limited fetches.
 */
@Environment(value=EnvType.CLIENT)
public interface FetchRateLimiter {
    /**
     * Notifies the tracker that the tracked task is run.
     */
    public void onRun();

    /**
     * {@return the time left before the next scheduled run of the tracked task}
     */
    public long getRemainingPeriod();
}

