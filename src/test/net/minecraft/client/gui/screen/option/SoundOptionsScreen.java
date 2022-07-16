/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SoundSliderWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.TranslatableText;

@Environment(value=EnvType.CLIENT)
public class SoundOptionsScreen
extends GameOptionsScreen {
    public SoundOptionsScreen(Screen parent, GameOptions options) {
        super(parent, options, new TranslatableText("options.sounds.title"));
    }

    @Override
    protected void init() {
        int i = 0;
        this.addDrawableChild(new SoundSliderWidget(this.client, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), SoundCategory.MASTER, 310));
        i += 2;
        for (SoundCategory soundCategory : SoundCategory.values()) {
            if (soundCategory == SoundCategory.MASTER) continue;
            this.addDrawableChild(new SoundSliderWidget(this.client, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), soundCategory, 150));
            ++i;
        }
        this.addDrawableChild(Option.SUBTITLES.createButton(this.gameOptions, this.width / 2 - 75, this.height / 6 - 12 + 24 * (++i >> 1), 150));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.DONE, button -> this.client.setScreen(this.parent)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        SoundOptionsScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
