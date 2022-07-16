/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.hud.spectator;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommandGroup;
import net.minecraft.client.gui.hud.spectator.TeamTeleportSpectatorMenu;
import net.minecraft.client.gui.hud.spectator.TeleportSpectatorMenu;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(value=EnvType.CLIENT)
public class RootSpectatorCommandGroup
implements SpectatorMenuCommandGroup {
    private static final Text PROMPT_TEXT = new TranslatableText("spectatorMenu.root.prompt");
    private final List<SpectatorMenuCommand> elements = Lists.newArrayList();

    public RootSpectatorCommandGroup() {
        this.elements.add(new TeleportSpectatorMenu());
        this.elements.add(new TeamTeleportSpectatorMenu());
    }

    @Override
    public List<SpectatorMenuCommand> getCommands() {
        return this.elements;
    }

    @Override
    public Text getPrompt() {
        return PROMPT_TEXT;
    }
}

