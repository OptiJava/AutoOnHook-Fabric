/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

public enum TickPriority {
    EXTREMELY_HIGH(-3),
    VERY_HIGH(-2),
    HIGH(-1),
    NORMAL(0),
    LOW(1),
    VERY_LOW(2),
    EXTREMELY_LOW(3);

    private final int index;

    private TickPriority(int index) {
        this.index = index;
    }

    public static TickPriority byIndex(int index) {
        for (TickPriority tickPriority : TickPriority.values()) {
            if (tickPriority.index != index) continue;
            return tickPriority;
        }
        if (index < TickPriority.EXTREMELY_HIGH.index) {
            return EXTREMELY_HIGH;
        }
        return EXTREMELY_LOW;
    }

    public int getIndex() {
        return this.index;
    }
}

