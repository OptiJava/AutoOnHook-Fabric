/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Narratable;

@Environment(value=EnvType.CLIENT)
public interface Selectable
extends Narratable {
    public SelectionType getType();

    default public boolean isNarratable() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SelectionType {
        NONE,
        HOVERED,
        FOCUSED;


        public boolean isFocused() {
            return this == FOCUSED;
        }
    }
}

