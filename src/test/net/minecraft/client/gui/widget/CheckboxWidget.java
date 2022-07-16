/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class CheckboxWidget
extends PressableWidget {
    private static final Identifier TEXTURE = new Identifier("textures/gui/checkbox.png");
    private static final int TEXT_COLOR = 0xE0E0E0;
    private boolean checked;
    private final boolean showMessage;

    public CheckboxWidget(int x, int y, int width, int height, Text message, boolean checked) {
        this(x, y, width, height, message, checked, true);
    }

    public CheckboxWidget(int x, int y, int width, int height, Text message, boolean checked, boolean showMessage) {
        super(x, y, width, height, message);
        this.checked = checked;
        this.showMessage = showMessage;
    }

    @Override
    public void onPress() {
        this.checked = !this.checked;
    }

    public boolean isChecked() {
        return this.checked;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, (Text)new TranslatableText("narration.checkbox.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, (Text)new TranslatableText("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        CheckboxWidget.drawTexture(matrices, this.x, this.y, this.isFocused() ? 20.0f : 0.0f, this.checked ? 20.0f : 0.0f, 20, this.height, 64, 64);
        this.renderBackground(matrices, minecraftClient, mouseX, mouseY);
        if (this.showMessage) {
            CheckboxWidget.drawTextWithShadow(matrices, textRenderer, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 0xE0E0E0 | MathHelper.ceil(this.alpha * 255.0f) << 24);
        }
    }
}

