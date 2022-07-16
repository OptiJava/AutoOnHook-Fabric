/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class RealmsParentalConsentScreen
extends RealmsScreen {
    private static final Text PRIVACY_INFO_TEXT = new TranslatableText("mco.account.privacyinfo");
    private final Screen parent;
    private MultilineText privacyInfoText = MultilineText.EMPTY;

    public RealmsParentalConsentScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }

    @Override
    public void init() {
        TranslatableText text = new TranslatableText("mco.account.update");
        Text text2 = ScreenTexts.BACK;
        int i = Math.max(this.textRenderer.getWidth(text), this.textRenderer.getWidth(text2)) + 30;
        TranslatableText text3 = new TranslatableText("mco.account.privacy.info");
        int j = (int)((double)this.textRenderer.getWidth(text3) * 1.2);
        this.addDrawableChild(new ButtonWidget(this.width / 2 - j / 2, RealmsParentalConsentScreen.row(11), j, 20, text3, button -> Util.getOperatingSystem().open("https://aka.ms/MinecraftGDPR")));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - (i + 5), RealmsParentalConsentScreen.row(13), i, 20, text, button -> Util.getOperatingSystem().open("https://aka.ms/UpdateMojangAccount")));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, RealmsParentalConsentScreen.row(13), i, 20, text2, button -> this.client.setScreen(this.parent)));
        this.privacyInfoText = MultilineText.create(this.textRenderer, (StringVisitable)PRIVACY_INFO_TEXT, (int)Math.round((double)this.width * 0.9));
    }

    @Override
    public Text getNarratedTitle() {
        return PRIVACY_INFO_TEXT;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.privacyInfoText.drawCenterWithShadow(matrices, this.width / 2, 15, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}

