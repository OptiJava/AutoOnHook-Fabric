/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.hud;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BossBarHud
extends DrawableHelper {
    private static final Identifier BARS_TEXTURE = new Identifier("textures/gui/bars.png");
    private static final int WIDTH = 182;
    private static final int field_32178 = 5;
    private static final int field_32179 = 80;
    private final MinecraftClient client;
    final Map<UUID, ClientBossBar> bossBars = Maps.newLinkedHashMap();

    public BossBarHud(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrices) {
        if (this.bossBars.isEmpty()) {
            return;
        }
        int i = this.client.getWindow().getScaledWidth();
        int j = 12;
        for (ClientBossBar clientBossBar : this.bossBars.values()) {
            int k = i / 2 - 91;
            int l = j;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, BARS_TEXTURE);
            this.renderBossBar(matrices, k, l, clientBossBar);
            Text text = clientBossBar.getName();
            int m = this.client.textRenderer.getWidth(text);
            int n = i / 2 - m / 2;
            int o = l - 9;
            this.client.textRenderer.drawWithShadow(matrices, text, (float)n, (float)o, 0xFFFFFF);
            if ((j += 10 + this.client.textRenderer.fontHeight) < this.client.getWindow().getScaledHeight() / 3) continue;
            break;
        }
    }

    private void renderBossBar(MatrixStack matrices, int x, int y, BossBar bossBar) {
        int i;
        this.drawTexture(matrices, x, y, 0, bossBar.getColor().ordinal() * 5 * 2, 182, 5);
        if (bossBar.getStyle() != BossBar.Style.PROGRESS) {
            this.drawTexture(matrices, x, y, 0, 80 + (bossBar.getStyle().ordinal() - 1) * 5 * 2, 182, 5);
        }
        if ((i = (int)(bossBar.getPercent() * 183.0f)) > 0) {
            this.drawTexture(matrices, x, y, 0, bossBar.getColor().ordinal() * 5 * 2 + 5, i, 5);
            if (bossBar.getStyle() != BossBar.Style.PROGRESS) {
                this.drawTexture(matrices, x, y, 0, 80 + (bossBar.getStyle().ordinal() - 1) * 5 * 2 + 5, i, 5);
            }
        }
    }

    public void handlePacket(BossBarS2CPacket packet) {
        packet.accept(new BossBarS2CPacket.Consumer(){

            @Override
            public void add(UUID uuid, Text name, float percent, BossBar.Color color, BossBar.Style style, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
                BossBarHud.this.bossBars.put(uuid, new ClientBossBar(uuid, name, percent, color, style, darkenSky, dragonMusic, thickenFog));
            }

            @Override
            public void remove(UUID uuid) {
                BossBarHud.this.bossBars.remove(uuid);
            }

            @Override
            public void updateProgress(UUID uuid, float percent) {
                BossBarHud.this.bossBars.get(uuid).setPercent(percent);
            }

            @Override
            public void updateName(UUID uuid, Text name) {
                BossBarHud.this.bossBars.get(uuid).setName(name);
            }

            @Override
            public void updateStyle(UUID id, BossBar.Color color, BossBar.Style style) {
                ClientBossBar clientBossBar = BossBarHud.this.bossBars.get(id);
                clientBossBar.setColor(color);
                clientBossBar.setStyle(style);
            }

            @Override
            public void updateProperties(UUID uuid, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
                ClientBossBar clientBossBar = BossBarHud.this.bossBars.get(uuid);
                clientBossBar.setDarkenSky(darkenSky);
                clientBossBar.setDragonMusic(dragonMusic);
                clientBossBar.setThickenFog(thickenFog);
            }
        });
    }

    public void clear() {
        this.bossBars.clear();
    }

    public boolean shouldPlayDragonMusic() {
        if (!this.bossBars.isEmpty()) {
            for (BossBar bossBar : this.bossBars.values()) {
                if (!bossBar.hasDragonMusic()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldDarkenSky() {
        if (!this.bossBars.isEmpty()) {
            for (BossBar bossBar : this.bossBars.values()) {
                if (!bossBar.shouldDarkenSky()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldThickenFog() {
        if (!this.bossBars.isEmpty()) {
            for (BossBar bossBar : this.bossBars.values()) {
                if (!bossBar.shouldThickenFog()) continue;
                return true;
            }
        }
        return false;
    }
}

