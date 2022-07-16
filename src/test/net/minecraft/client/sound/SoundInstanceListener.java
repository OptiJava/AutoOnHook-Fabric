/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;

@Environment(value=EnvType.CLIENT)
public interface SoundInstanceListener {
    public void onSoundPlayed(SoundInstance var1, WeightedSoundSet var2);
}

