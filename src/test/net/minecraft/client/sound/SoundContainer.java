/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundSystem;

@Environment(value=EnvType.CLIENT)
public interface SoundContainer<T> {
    public int getWeight();

    public T getSound();

    public void preload(SoundSystem var1);
}

