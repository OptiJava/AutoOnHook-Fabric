/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.item;

public interface TooltipContext {
    public boolean isAdvanced();

    public static enum Default implements TooltipContext
    {
        NORMAL(false),
        ADVANCED(true);

        private final boolean advanced;

        private Default(boolean advanced) {
            this.advanced = advanced;
        }

        @Override
        public boolean isAdvanced() {
            return this.advanced;
        }
    }
}

