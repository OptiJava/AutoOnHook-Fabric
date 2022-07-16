/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.hud;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for rendering the player list while the {@linkplain
 * net.minecraft.client.option.GameOptions#keyPlayerList player list
 * key} is pressed.
 * 
 * <p>The current instance used by the client can be obtained by {@code
 * MinecraftClient.getInstance().inGameHud.getPlayerListHud()}.
 */
@Environment(value=EnvType.CLIENT)
public class PlayerListHud
extends DrawableHelper {
    private static final Ordering<PlayerListEntry> ENTRY_ORDERING = Ordering.from(new EntryOrderComparator());
    public static final int MAX_ROWS = 20;
    public static final int HEART_OUTLINE_U = 16;
    public static final int BLINKING_HEART_OUTLINE_U = 25;
    public static final int HEART_U = 52;
    public static final int HALF_HEART_U = 61;
    public static final int GOLDEN_HEART_U = 160;
    public static final int HALF_GOLDEN_HEART_U = 169;
    public static final int BLINKING_HEART_U = 70;
    public static final int BLINKING_HALF_HEART_U = 79;
    private final MinecraftClient client;
    private final InGameHud inGameHud;
    @Nullable
    private Text footer;
    @Nullable
    private Text header;
    /**
     * The time, in milliseconds, when this HUD was last set to visible.
     */
    private long showTime;
    private boolean visible;

    public PlayerListHud(MinecraftClient client, InGameHud inGameHud) {
        this.client = client;
        this.inGameHud = inGameHud;
    }

    /**
     * {@return the player name rendered by this HUD}
     */
    public Text getPlayerName(PlayerListEntry entry) {
        if (entry.getDisplayName() != null) {
            return this.applyGameModeFormatting(entry, entry.getDisplayName().shallowCopy());
        }
        return this.applyGameModeFormatting(entry, Team.decorateName(entry.getScoreboardTeam(), new LiteralText(entry.getProfile().getName())));
    }

    /**
     * {@linkplain net.minecraft.util.Formatting#ITALIC Italicizes} the given text if
     * the given player is in {@linkplain net.minecraft.world.GameMode#SPECTATOR spectator mode}.
     */
    private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
        return entry.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
    }

    public void setVisible(boolean visible) {
        if (visible && !this.visible) {
            this.showTime = Util.getMeasuringTimeMs();
        }
        this.visible = visible;
    }

    public void render(MatrixStack matrices, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective objective) {
        int s;
        int r;
        boolean bl;
        int l;
        int k;
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;
        List<PlayerListEntry> list = ENTRY_ORDERING.sortedCopy(clientPlayNetworkHandler.getPlayerList());
        int i = 0;
        int j = 0;
        for (PlayerListEntry playerListEntry : list) {
            k = this.client.textRenderer.getWidth(this.getPlayerName(playerListEntry));
            i = Math.max(i, k);
            if (objective == null || objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) continue;
            k = this.client.textRenderer.getWidth(" " + scoreboard.getPlayerScore(playerListEntry.getProfile().getName(), objective).getScore());
            j = Math.max(j, k);
        }
        list = list.subList(0, Math.min(list.size(), 80));
        int playerListEntry = l = list.size();
        k = 1;
        while (playerListEntry > 20) {
            playerListEntry = (l + ++k - 1) / k;
        }
        boolean bl2 = bl = this.client.isInSingleplayer() || this.client.getNetworkHandler().getConnection().isEncrypted();
        int m = objective != null ? (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS ? 90 : j) : 0;
        int n = Math.min(k * ((bl ? 9 : 0) + i + m + 13), scaledWindowWidth - 50) / k;
        int o = scaledWindowWidth / 2 - (n * k + (k - 1) * 5) / 2;
        int p = 10;
        int q = n * k + (k - 1) * 5;
        List<OrderedText> list2 = null;
        if (this.header != null) {
            list2 = this.client.textRenderer.wrapLines(this.header, scaledWindowWidth - 50);
            for (OrderedText orderedText : list2) {
                q = Math.max(q, this.client.textRenderer.getWidth(orderedText));
            }
        }
        List<OrderedText> list3 = null;
        if (this.footer != null) {
            list3 = this.client.textRenderer.wrapLines(this.footer, scaledWindowWidth - 50);
            for (OrderedText orderedText2 : list3) {
                q = Math.max(q, this.client.textRenderer.getWidth(orderedText2));
            }
        }
        if (list2 != null) {
            PlayerListHud.fill(matrices, scaledWindowWidth / 2 - q / 2 - 1, p - 1, scaledWindowWidth / 2 + q / 2 + 1, p + list2.size() * this.client.textRenderer.fontHeight, Integer.MIN_VALUE);
            for (OrderedText orderedText2 : list2) {
                r = this.client.textRenderer.getWidth(orderedText2);
                this.client.textRenderer.drawWithShadow(matrices, orderedText2, (float)(scaledWindowWidth / 2 - r / 2), (float)p, -1);
                p += this.client.textRenderer.fontHeight;
            }
            ++p;
        }
        PlayerListHud.fill(matrices, scaledWindowWidth / 2 - q / 2 - 1, p - 1, scaledWindowWidth / 2 + q / 2 + 1, p + playerListEntry * 9, Integer.MIN_VALUE);
        int n2 = this.client.options.getTextBackgroundColor(0x20FFFFFF);
        for (int orderedText2 = 0; orderedText2 < l; ++orderedText2) {
            int playerEntity;
            int bl22;
            r = orderedText2 / playerListEntry;
            s = orderedText2 % playerListEntry;
            int t = o + r * n + r * 5;
            int u = p + s * 9;
            PlayerListHud.fill(matrices, t, u, t + n, u + 8, n2);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (orderedText2 >= list.size()) continue;
            PlayerListEntry playerListEntry2 = list.get(orderedText2);
            GameProfile gameProfile = playerListEntry2.getProfile();
            if (bl) {
                PlayerEntity playerEntity2 = this.client.world.getPlayerByUuid(gameProfile.getId());
                boolean bl222 = playerEntity2 != null && playerEntity2.isPartVisible(PlayerModelPart.CAPE) && ("Dinnerbone".equals(gameProfile.getName()) || "Grumm".equals(gameProfile.getName()));
                RenderSystem.setShaderTexture(0, playerListEntry2.getSkinTexture());
                int v = 8 + (bl222 ? 8 : 0);
                int w = 8 * (bl222 ? -1 : 1);
                DrawableHelper.drawTexture(matrices, t, u, 8, 8, 8.0f, v, 8, w, 64, 64);
                if (playerEntity2 != null && playerEntity2.isPartVisible(PlayerModelPart.HAT)) {
                    int x = 8 + (bl222 ? 8 : 0);
                    int y = 8 * (bl222 ? -1 : 1);
                    DrawableHelper.drawTexture(matrices, t, u, 8, 8, 40.0f, x, 8, y, 64, 64);
                }
                t += 9;
            }
            this.client.textRenderer.drawWithShadow(matrices, this.getPlayerName(playerListEntry2), (float)t, (float)u, playerListEntry2.getGameMode() == GameMode.SPECTATOR ? -1862270977 : -1);
            if (objective != null && playerListEntry2.getGameMode() != GameMode.SPECTATOR && (bl22 = (playerEntity = t + i + 1) + m) - playerEntity > 5) {
                this.renderScoreboardObjective(objective, u, gameProfile.getName(), playerEntity, bl22, playerListEntry2, matrices);
            }
            this.renderLatencyIcon(matrices, n, t - (bl ? 9 : 0), u, playerListEntry2);
        }
        if (list3 != null) {
            PlayerListHud.fill(matrices, scaledWindowWidth / 2 - q / 2 - 1, (p += playerListEntry * 9 + 1) - 1, scaledWindowWidth / 2 + q / 2 + 1, p + list3.size() * this.client.textRenderer.fontHeight, Integer.MIN_VALUE);
            for (OrderedText r2 : list3) {
                s = this.client.textRenderer.getWidth(r2);
                this.client.textRenderer.drawWithShadow(matrices, r2, (float)(scaledWindowWidth / 2 - s / 2), (float)p, -1);
                p += this.client.textRenderer.fontHeight;
            }
        }
    }

    protected void renderLatencyIcon(MatrixStack matrices, int width, int x, int y, PlayerListEntry entry) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        boolean i = false;
        int j = entry.getLatency() < 0 ? 5 : (entry.getLatency() < 150 ? 0 : (entry.getLatency() < 300 ? 1 : (entry.getLatency() < 600 ? 2 : (entry.getLatency() < 1000 ? 3 : 4))));
        this.setZOffset(this.getZOffset() + 100);
        this.drawTexture(matrices, x + width - 11, y, 0, 176 + j * 8, 10, 8);
        this.setZOffset(this.getZOffset() - 100);
    }

    private void renderScoreboardObjective(ScoreboardObjective objective, int y, String player, int startX, int endX, PlayerListEntry entry, MatrixStack matrices) {
        int i = objective.getScoreboard().getPlayerScore(player, objective).getScore();
        if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
            boolean bl;
            RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
            long l = Util.getMeasuringTimeMs();
            if (this.showTime == entry.getShowTime()) {
                if (i < entry.getLastHealth()) {
                    entry.setLastHealthTime(l);
                    entry.setBlinkingHeartTime(this.inGameHud.getTicks() + 20);
                } else if (i > entry.getLastHealth()) {
                    entry.setLastHealthTime(l);
                    entry.setBlinkingHeartTime(this.inGameHud.getTicks() + 10);
                }
            }
            if (l - entry.getLastHealthTime() > 1000L || this.showTime != entry.getShowTime()) {
                entry.setLastHealth(i);
                entry.setHealth(i);
                entry.setLastHealthTime(l);
            }
            entry.setShowTime(this.showTime);
            entry.setLastHealth(i);
            int j = MathHelper.ceil((float)Math.max(i, entry.getHealth()) / 2.0f);
            int k = Math.max(MathHelper.ceil(i / 2), Math.max(MathHelper.ceil(entry.getHealth() / 2), 10));
            boolean bl2 = bl = entry.getBlinkingHeartTime() > (long)this.inGameHud.getTicks() && (entry.getBlinkingHeartTime() - (long)this.inGameHud.getTicks()) / 3L % 2L == 1L;
            if (j > 0) {
                int m = MathHelper.floor(Math.min((float)(endX - startX - 4) / (float)k, 9.0f));
                if (m > 3) {
                    int n;
                    for (n = j; n < k; ++n) {
                        this.drawTexture(matrices, startX + n * m, y, bl ? 25 : 16, 0, 9, 9);
                    }
                    for (n = 0; n < j; ++n) {
                        this.drawTexture(matrices, startX + n * m, y, bl ? 25 : 16, 0, 9, 9);
                        if (bl) {
                            if (n * 2 + 1 < entry.getHealth()) {
                                this.drawTexture(matrices, startX + n * m, y, 70, 0, 9, 9);
                            }
                            if (n * 2 + 1 == entry.getHealth()) {
                                this.drawTexture(matrices, startX + n * m, y, 79, 0, 9, 9);
                            }
                        }
                        if (n * 2 + 1 < i) {
                            this.drawTexture(matrices, startX + n * m, y, n >= 10 ? 160 : 52, 0, 9, 9);
                        }
                        if (n * 2 + 1 != i) continue;
                        this.drawTexture(matrices, startX + n * m, y, n >= 10 ? 169 : 61, 0, 9, 9);
                    }
                } else {
                    float n = MathHelper.clamp((float)i / 20.0f, 0.0f, 1.0f);
                    int o = (int)((1.0f - n) * 255.0f) << 16 | (int)(n * 255.0f) << 8;
                    String string = "" + (float)i / 2.0f;
                    if (endX - this.client.textRenderer.getWidth(string + "hp") >= startX) {
                        string = string + "hp";
                    }
                    this.client.textRenderer.drawWithShadow(matrices, string, (float)((endX + startX) / 2 - this.client.textRenderer.getWidth(string) / 2), (float)y, o);
                }
            }
        } else {
            String l = "" + Formatting.YELLOW + i;
            this.client.textRenderer.drawWithShadow(matrices, l, (float)(endX - this.client.textRenderer.getWidth(l)), (float)y, 0xFFFFFF);
        }
    }

    public void setFooter(@Nullable Text footer) {
        this.footer = footer;
    }

    public void setHeader(@Nullable Text header) {
        this.header = header;
    }

    public void clear() {
        this.header = null;
        this.footer = null;
    }

    @Environment(value=EnvType.CLIENT)
    static class EntryOrderComparator
    implements Comparator<PlayerListEntry> {
        EntryOrderComparator() {
        }

        @Override
        public int compare(PlayerListEntry playerListEntry, PlayerListEntry playerListEntry2) {
            Team team = playerListEntry.getScoreboardTeam();
            Team team2 = playerListEntry2.getScoreboardTeam();
            return ComparisonChain.start().compareTrueFirst(playerListEntry.getGameMode() != GameMode.SPECTATOR, playerListEntry2.getGameMode() != GameMode.SPECTATOR).compare((Comparable<?>)((Object)(team != null ? team.getName() : "")), (Comparable<?>)((Object)(team2 != null ? team2.getName() : ""))).compare(playerListEntry.getProfile().getName(), playerListEntry2.getProfile().getName(), String::compareToIgnoreCase).result();
        }

        @Override
        public /* synthetic */ int compare(Object a, Object b) {
            return this.compare((PlayerListEntry)a, (PlayerListEntry)b);
        }
    }
}

