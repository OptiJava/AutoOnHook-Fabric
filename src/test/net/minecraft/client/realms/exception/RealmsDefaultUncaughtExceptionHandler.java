/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsDefaultUncaughtExceptionHandler
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public RealmsDefaultUncaughtExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        this.logger.error("Caught previously unhandled exception :");
        this.logger.error(e);
    }
}
