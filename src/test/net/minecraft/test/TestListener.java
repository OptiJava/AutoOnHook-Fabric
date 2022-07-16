/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;

public interface TestListener {
    public void onStarted(GameTestState var1);

    public void onPassed(GameTestState var1);

    public void onFailed(GameTestState var1);
}

