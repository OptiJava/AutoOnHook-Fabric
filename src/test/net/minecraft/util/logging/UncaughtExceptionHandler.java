/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.logging;

import org.apache.logging.log4j.Logger;

public class UncaughtExceptionHandler
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public UncaughtExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        this.logger.error("Caught previously unhandled exception :");
        this.logger.error(thread.getName(), throwable);
    }
}

