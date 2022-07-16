/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.hud.spectator;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public interface SpectatorMenuCommandGroup {
    public List<SpectatorMenuCommand> getCommands();

    public Text getPrompt();
}

