/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;

class NotEnoughSuccessesError
extends Throwable {
    public NotEnoughSuccessesError(int attempts, int successes, GameTestState test) {
        super("Not enough successes: " + successes + " out of " + attempts + " attempts. Required successes: " + test.getRequiredSuccesses() + ". max attempts: " + test.getMaxAttempts() + ".", test.getThrowable());
    }
}

