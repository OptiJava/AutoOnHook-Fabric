/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

@Environment(value=EnvType.CLIENT)
public abstract class MovingSoundInstance
extends AbstractSoundInstance
implements TickableSoundInstance {
    private boolean done;

    protected MovingSoundInstance(SoundEvent soundEvent, SoundCategory soundCategory) {
        super(soundEvent, soundCategory);
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    protected final void setDone() {
        this.done = true;
        this.repeat = false;
    }
}

