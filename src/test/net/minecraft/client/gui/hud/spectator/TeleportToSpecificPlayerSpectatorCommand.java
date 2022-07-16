/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.hud.spectator;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class TeleportToSpecificPlayerSpectatorCommand
implements SpectatorMenuCommand {
    private final GameProfile gameProfile;
    private final Identifier skinId;
    private final Text name;

    public TeleportToSpecificPlayerSpectatorCommand(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraftClient.getSkinProvider().getTextures(gameProfile);
        this.skinId = map.containsKey((Object)MinecraftProfileTexture.Type.SKIN) ? minecraftClient.getSkinProvider().loadSkin(map.get((Object)MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN) : DefaultSkinHelper.getTexture(PlayerEntity.getUuidFromProfile(gameProfile));
        this.name = new LiteralText(gameProfile.getName());
    }

    @Override
    public void use(SpectatorMenu menu) {
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new SpectatorTeleportC2SPacket(this.gameProfile.getId()));
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public void renderIcon(MatrixStack matrices, float f, int i) {
        RenderSystem.setShaderTexture(0, this.skinId);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float)i / 255.0f);
        DrawableHelper.drawTexture(matrices, 2, 2, 12, 12, 8.0f, 8.0f, 8, 8, 64, 64);
        DrawableHelper.drawTexture(matrices, 2, 2, 12, 12, 40.0f, 8.0f, 8, 8, 64, 64);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

