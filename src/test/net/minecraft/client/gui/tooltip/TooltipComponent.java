/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.Matrix4f;

@Environment(value=EnvType.CLIENT)
public interface TooltipComponent {
    public static TooltipComponent of(OrderedText text) {
        return new OrderedTextTooltipComponent(text);
    }

    public static TooltipComponent of(TooltipData data) {
        if (data instanceof BundleTooltipData) {
            return new BundleTooltipComponent((BundleTooltipData)data);
        }
        throw new IllegalArgumentException("Unknown TooltipComponent");
    }

    public int getHeight();

    public int getWidth(TextRenderer var1);

    default public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
    }

    default public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
    }
}

