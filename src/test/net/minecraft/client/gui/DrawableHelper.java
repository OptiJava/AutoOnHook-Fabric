/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class DrawableHelper {
    /**
     * The texture used by options for background.
     */
    public static final Identifier OPTIONS_BACKGROUND_TEXTURE = new Identifier("textures/gui/options_background.png");
    /**
     * The texture of icons used in the stats screen.
     */
    public static final Identifier STATS_ICON_TEXTURE = new Identifier("textures/gui/container/stats_icons.png");
    /**
     * The texture of various icons and widgets used for rendering ingame indicators.
     */
    public static final Identifier GUI_ICONS_TEXTURE = new Identifier("textures/gui/icons.png");
    /**
     * The z offset used by {@link DrawableHelper}.
     */
    private int zOffset;

    protected void drawHorizontalLine(MatrixStack matrices, int x1, int x2, int y, int color) {
        if (x2 < x1) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }
        DrawableHelper.fill(matrices, x1, y, x2 + 1, y + 1, color);
    }

    protected void drawVerticalLine(MatrixStack matrices, int x, int y1, int y2, int color) {
        if (y2 < y1) {
            int i = y1;
            y1 = y2;
            y2 = i;
        }
        DrawableHelper.fill(matrices, x, y1 + 1, x + 1, y2, color);
    }

    public static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(matrices.peek().getModel(), x1, y1, x2, y2, color);
    }

    private static void fill(Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float i2 = (float)(color >> 24 & 0xFF) / 255.0f;
        float f = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float h = (float)(color & 0xFF) / 255.0f;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(f, g, h, i2).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(f, g, h, i2).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(f, g, h, i2).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(f, g, h, i2).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void fillGradient(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        DrawableHelper.fillGradient(matrices, startX, startY, endX, endY, colorStart, colorEnd, this.zOffset);
    }

    protected static void fillGradient(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        DrawableHelper.fillGradient(matrices.peek().getModel(), bufferBuilder, startX, startY, endX, endY, z, colorStart, colorEnd);
        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    protected static void fillGradient(Matrix4f matrix, BufferBuilder bufferBuilder, int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
        float f = (float)(colorStart >> 24 & 0xFF) / 255.0f;
        float g = (float)(colorStart >> 16 & 0xFF) / 255.0f;
        float h = (float)(colorStart >> 8 & 0xFF) / 255.0f;
        float i = (float)(colorStart & 0xFF) / 255.0f;
        float j = (float)(colorEnd >> 24 & 0xFF) / 255.0f;
        float k = (float)(colorEnd >> 16 & 0xFF) / 255.0f;
        float l = (float)(colorEnd >> 8 & 0xFF) / 255.0f;
        float m = (float)(colorEnd & 0xFF) / 255.0f;
        bufferBuilder.vertex(matrix, endX, startY, z).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, startX, startY, z).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, startX, endY, z).color(k, l, m, j).next();
        bufferBuilder.vertex(matrix, endX, endY, z).color(k, l, m, j).next();
    }

    public static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, String text, int centerX, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
    }

    public static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        OrderedText orderedText = text.asOrderedText();
        textRenderer.drawWithShadow(matrices, orderedText, (float)(centerX - textRenderer.getWidth(orderedText) / 2), (float)y, color);
    }

    public static void drawCenteredTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, OrderedText text, int centerX, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrices, TextRenderer textRenderer, String text, int x, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, (float)x, (float)y, color);
    }

    public static void drawWithShadow(MatrixStack matrices, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, (float)x, (float)y, color);
    }

    public static void drawTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, (float)x, (float)y, color);
    }

    /**
     * @param renderAction the action to render both the content and the outline, taking x and y positions as input
     */
    public void drawWithOutline(int x, int y, BiConsumer<Integer, Integer> renderAction) {
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        renderAction.accept(x + 1, y);
        renderAction.accept(x - 1, y);
        renderAction.accept(x, y + 1);
        renderAction.accept(x, y - 1);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        renderAction.accept(x, y);
    }

    public static void drawSprite(MatrixStack matrices, int x, int y, int z, int width, int height, Sprite sprite) {
        DrawableHelper.drawTexturedQuad(matrices.peek().getModel(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    /**
     * Draws a textured rectangle from a region in a 256x256 texture.
     * 
     * <p>The Z coordinate of the rectangle is {@link #zOffset}.
     * 
     * <p>The width and height of the region are the same as
     * the dimensions of the rectangle.
     * 
     * @param matrices the matrix stack used for rendering
     * @param x the X coordinate of the rectangle
     * @param y the Y coordinate of the rectangle
     * @param u the left-most coordinate of the texture region
     * @param v the top-most coordinate of the texture region
     * @param width the width
     * @param height the height
     */
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        DrawableHelper.drawTexture(matrices, x, y, this.zOffset, u, v, width, height, 256, 256);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
     * 
     * <p>The width and height of the region are the same as
     * the dimensions of the rectangle.
     * 
     * @param matrices the matrix stack used for rendering
     * @param x the X coordinate of the rectangle
     * @param y the Y coordinate of the rectangle
     * @param z the Z coordinate of the rectangle
     * @param u the left-most coordinate of the texture region
     * @param v the top-most coordinate of the texture region
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param textureHeight the height of the entire texture
     * @param textureWidth the width of the entire texture
     */
    public static void drawTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height, int textureHeight, int textureWidth) {
        DrawableHelper.drawTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
     * 
     * @param matrices the matrix stack used for rendering
     * @param x the X coordinate of the rectangle
     * @param y the Y coordinate of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param u the left-most coordinate of the texture region
     * @param v the top-most coordinate of the texture region
     * @param regionWidth the width of the texture region
     * @param regionHeight the height of the texture region
     * @param textureWidth the width of the entire texture
     * @param textureHeight the height of the entire texture
     */
    public static void drawTexture(MatrixStack matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        DrawableHelper.drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
     * 
     * <p>The width and height of the region are the same as
     * the dimensions of the rectangle.
     * 
     * @param matrices the matrix stack used for rendering
     * @param x the X coordinate of the rectangle
     * @param y the Y coordinate of the rectangle
     * @param u the left-most coordinate of the texture region
     * @param v the top-most coordinate of the texture region
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param textureWidth the width of the entire texture
     * @param textureHeight the height of the entire texture
     */
    public static void drawTexture(MatrixStack matrices, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    private static void drawTexture(MatrixStack matrices, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        DrawableHelper.drawTexturedQuad(matrices.peek().getModel(), x0, x1, y0, y1, z, (u + 0.0f) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0f) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    private static void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x0, y1, z).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x1, y1, z).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x1, y0, z).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x0, y0, z).texture(u0, v0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    public int getZOffset() {
        return this.zOffset;
    }

    public void setZOffset(int zOffset) {
        this.zOffset = zOffset;
    }
}

