/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class BackupPromptScreen
extends Screen {
    @Nullable
    private final Screen parent;
    protected final Callback callback;
    private final Text subtitle;
    private final boolean showEraseCacheCheckbox;
    private MultilineText wrappedText = MultilineText.EMPTY;
    protected int field_32236;
    private CheckboxWidget eraseCacheCheckbox;

    public BackupPromptScreen(@Nullable Screen parent, Callback callback, Text title, Text subtitle, boolean showEraseCacheCheckBox) {
        super(title);
        this.parent = parent;
        this.callback = callback;
        this.subtitle = subtitle;
        this.showEraseCacheCheckbox = showEraseCacheCheckBox;
    }

    @Override
    protected void init() {
        super.init();
        this.wrappedText = MultilineText.create(this.textRenderer, (StringVisitable)this.subtitle, this.width - 50);
        int i = (this.wrappedText.count() + 1) * this.textRenderer.fontHeight;
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, 100 + i, 150, 20, new TranslatableText("selectWorld.backupJoinConfirmButton"), button -> this.callback.proceed(true, this.eraseCacheCheckbox.isChecked())));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155 + 160, 100 + i, 150, 20, new TranslatableText("selectWorld.backupJoinSkipButton"), button -> this.callback.proceed(false, this.eraseCacheCheckbox.isChecked())));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155 + 80, 124 + i, 150, 20, ScreenTexts.CANCEL, button -> this.client.setScreen(this.parent)));
        this.eraseCacheCheckbox = new CheckboxWidget(this.width / 2 - 155 + 80, 76 + i, 150, 20, new TranslatableText("selectWorld.backupEraseCache"), false);
        if (this.showEraseCacheCheckbox) {
            this.addDrawableChild(this.eraseCacheCheckbox);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        BackupPromptScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 50, 0xFFFFFF);
        this.wrappedText.drawCenterWithShadow(matrices, this.width / 2, 70);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Callback {
        public void proceed(boolean var1, boolean var2);
    }
}

