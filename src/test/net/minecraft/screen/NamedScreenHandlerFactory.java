/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen;

import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.text.Text;

public interface NamedScreenHandlerFactory
extends ScreenHandlerFactory {
    /**
     * Returns the title of this screen handler; will be a part of the open
     * screen packet sent to the client.
     */
    public Text getDisplayName();
}

