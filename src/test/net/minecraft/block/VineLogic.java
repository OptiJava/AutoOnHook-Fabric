/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.BlockState;

public class VineLogic {
    private static final double field_31198 = 0.826;
    public static final double field_31197 = 0.1;

    public static boolean isValidForWeepingStem(BlockState state) {
        return state.isAir();
    }

    public static int getGrowthLength(Random random) {
        double d = 1.0;
        int i = 0;
        while (random.nextDouble() < d) {
            d *= 0.826;
            ++i;
        }
        return i;
    }
}

