/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

@Environment(value=EnvType.CLIENT)
public interface Narratable {
    public void appendNarrations(NarrationMessageBuilder var1);
}
