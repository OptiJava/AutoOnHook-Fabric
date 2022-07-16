/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class ChatHudLine<T> {
    private final int creationTick;
    private final T text;
    private final int id;

    public ChatHudLine(int creationTick, T text, int id) {
        this.text = text;
        this.creationTick = creationTick;
        this.id = id;
    }

    public T getText() {
        return this.text;
    }

    public int getCreationTick() {
        return this.creationTick;
    }

    public int getId() {
        return this.id;
    }
}

