/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.EmptyGlyphRenderer;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the rendering of text.
 * 
 * <p>The current instance used by the client can be obtained by
 * {@code MinecraftClient.getInstance().textRenderer}.
 * 
 * @see net.minecraft.client.MinecraftClient#textRenderer
 */
@Environment(value=EnvType.CLIENT)
public class TextRenderer {
    private static final float field_32166 = 0.01f;
    private static final Vec3f FORWARD_SHIFT = new Vec3f(0.0f, 0.0f, 0.03f);
    /**
     * The font height of the text that is rendered by the text renderer.
     */
    public final int fontHeight = 9;
    public final Random random = new Random();
    private final Function<Identifier, FontStorage> fontStorageAccessor;
    private final TextHandler handler;

    public TextRenderer(Function<Identifier, FontStorage> fontStorageAccessor) {
        this.fontStorageAccessor = fontStorageAccessor;
        this.handler = new TextHandler((i, style) -> this.getFontStorage(style.getFont()).getGlyph(i).getAdvance(style.isBold()));
    }

    FontStorage getFontStorage(Identifier id) {
        return this.fontStorageAccessor.apply(id);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int drawWithShadow(MatrixStack matrices, String text, float x, float y, int color) {
        return this.draw(text, x, y, color, matrices.peek().getModel(), true, this.isRightToLeft());
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int drawWithShadow(MatrixStack matrices, String text, float x, float y, int color, boolean rightToLeft) {
        return this.draw(text, x, y, color, matrices.peek().getModel(), true, rightToLeft);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int draw(MatrixStack matrices, String text, float x, float y, int color) {
        return this.draw(text, x, y, color, matrices.peek().getModel(), false, this.isRightToLeft());
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int drawWithShadow(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return this.draw(text, x, y, color, matrices.peek().getModel(), true);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int drawWithShadow(MatrixStack matrices, Text text, float x, float y, int color) {
        return this.draw(text.asOrderedText(), x, y, color, matrices.peek().getModel(), true);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int draw(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return this.draw(text, x, y, color, matrices.peek().getModel(), false);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int draw(MatrixStack matrices, Text text, float x, float y, int color) {
        return this.draw(text.asOrderedText(), x, y, color, matrices.peek().getModel(), false);
    }

    public String mirror(String text) {
        try {
            Bidi bidi = new Bidi(new ArabicShaping(8).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException arabicShapingException) {
            return text;
        }
    }

    private int draw(String text, float x, float y, int color, Matrix4f matrix, boolean shadow, boolean mirror) {
        if (text == null) {
            return 0;
        }
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        int i = this.draw(text, x, y, color, shadow, matrix, immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE, mirror);
        immediate.draw();
        return i;
    }

    private int draw(OrderedText text, float x, float y, int color, Matrix4f matrix, boolean shadow) {
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        int i = this.draw(text, x, y, color, shadow, matrix, (VertexConsumerProvider)immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        immediate.draw();
        return i;
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        return this.draw(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light, this.isRightToLeft());
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean rightToLeft) {
        return this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light, rightToLeft);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int draw(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        return this.draw(text.asOrderedText(), x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public int draw(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        return this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light);
    }

    /**
     * @param color the text color in 0xAARRGGBB
     * @param outlineColor the outline color in 0xAARRGGBB
     */
    public void drawWithOutline(OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix, VertexConsumerProvider vertexConsumers, int light) {
        int i = TextRenderer.tweakTransparency(outlineColor);
        Drawer drawer = new Drawer(vertexConsumers, 0.0f, 0.0f, i, false, matrix, TextLayerType.NORMAL, light);
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                if (j == 0 && k == 0) continue;
                float[] fs = new float[]{x};
                int l2 = j;
                int m2 = k;
                text.accept((l, style, m) -> {
                    boolean bl = style.isBold();
                    FontStorage fontStorage = this.getFontStorage(style.getFont());
                    Glyph glyph = fontStorage.getGlyph(m);
                    drawer.x = fs[0] + (float)l2 * glyph.getShadowOffset();
                    drawer.y = y + (float)m2 * glyph.getShadowOffset();
                    fs[0] = fs[0] + glyph.getAdvance(bl);
                    return drawer.accept(l, style.withColor(i), m);
                });
            }
        }
        Drawer j = new Drawer(vertexConsumers, x, y, TextRenderer.tweakTransparency(color), false, matrix, TextLayerType.POLYGON_OFFSET, light);
        text.accept(j);
        j.drawLayer(0, x);
    }

    private static int tweakTransparency(int argb) {
        if ((argb & 0xFC000000) == 0) {
            return argb | 0xFF000000;
        }
        return argb;
    }

    private int drawInternal(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean mirror) {
        if (mirror) {
            text = this.mirror(text);
        }
        color = TextRenderer.tweakTransparency(color);
        Matrix4f matrix4f = matrix.copy();
        if (shadow) {
            this.drawLayer(text, x, y, color, true, matrix, vertexConsumers, seeThrough, backgroundColor, light);
            matrix4f.addToLastColumn(FORWARD_SHIFT);
        }
        x = this.drawLayer(text, x, y, color, false, matrix4f, vertexConsumers, seeThrough, backgroundColor, light);
        return (int)x + (shadow ? 1 : 0);
    }

    private int drawInternal(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int backgroundColor, int light) {
        color = TextRenderer.tweakTransparency(color);
        Matrix4f matrix4f = matrix.copy();
        if (shadow) {
            this.drawLayer(text, x, y, color, true, matrix, vertexConsumerProvider, seeThrough, backgroundColor, light);
            matrix4f.addToLastColumn(FORWARD_SHIFT);
        }
        x = this.drawLayer(text, x, y, color, false, matrix4f, vertexConsumerProvider, seeThrough, backgroundColor, light);
        return (int)x + (shadow ? 1 : 0);
    }

    private float drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
        Drawer drawer = new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, seeThrough, light);
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (CharacterVisitor)drawer);
        return drawer.drawLayer(underlineColor, x);
    }

    private float drawLayer(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
        Drawer drawer = new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, seeThrough, light);
        text.accept(drawer);
        return drawer.drawLayer(underlineColor, x);
    }

    void drawGlyph(GlyphRenderer glyphRenderer, boolean bold, boolean italic, float weight, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
        glyphRenderer.draw(italic, x, y, matrix, vertexConsumer, red, green, blue, alpha, light);
        if (bold) {
            glyphRenderer.draw(italic, x + weight, y, matrix, vertexConsumer, red, green, blue, alpha, light);
        }
    }

    /**
     * Gets the width of some text when rendered.
     * 
     * @param text the text
     */
    public int getWidth(String text) {
        return MathHelper.ceil(this.handler.getWidth(text));
    }

    /**
     * Gets the width of some text when rendered.
     * 
     * @param text the text
     */
    public int getWidth(StringVisitable text) {
        return MathHelper.ceil(this.handler.getWidth(text));
    }

    /**
     * Gets the width of some text when rendered.
     */
    public int getWidth(OrderedText text) {
        return MathHelper.ceil(this.handler.getWidth(text));
    }

    /**
     * Trims a string to be at most {@code maxWidth} wide.
     * 
     * @return the trimmed string
     */
    public String trimToWidth(String text, int maxWidth, boolean backwards) {
        return backwards ? this.handler.trimToWidthBackwards(text, maxWidth, Style.EMPTY) : this.handler.trimToWidth(text, maxWidth, Style.EMPTY);
    }

    /**
     * Trims a string to be at most {@code maxWidth} wide.
     * 
     * @return the trimmed string
     * @see TextHandler#trimToWidth(String, int, Style)
     */
    public String trimToWidth(String text, int maxWidth) {
        return this.handler.trimToWidth(text, maxWidth, Style.EMPTY);
    }

    /**
     * Trims a string to be at most {@code maxWidth} wide.
     * 
     * @return the text
     * @see TextHandler#trimToWidth(StringVisitable, int, Style)
     */
    public StringVisitable trimToWidth(StringVisitable text, int width) {
        return this.handler.trimToWidth(text, width, Style.EMPTY);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public void drawTrimmed(StringVisitable text, int x, int y, int maxWidth, int color) {
        Matrix4f matrix4f = AffineTransformation.identity().getMatrix();
        for (OrderedText orderedText : this.wrapLines(text, maxWidth)) {
            this.draw(orderedText, x, y, color, matrix4f, false);
            y += 9;
        }
    }

    /**
     * Gets the height of the text when it has been wrapped.
     * 
     * @return the height of the wrapped text
     * @see TextRenderer#wrapLines(StringVisitable, int)
     */
    public int getWrappedLinesHeight(String text, int maxWidth) {
        return 9 * this.handler.wrapLines(text, maxWidth, Style.EMPTY).size();
    }

    /**
     * Wraps text when the rendered width of text exceeds the {@code width}.
     * 
     * @return a list of ordered text which has been wrapped
     */
    public List<OrderedText> wrapLines(StringVisitable text, int width) {
        return Language.getInstance().reorder(this.handler.wrapLines(text, width, Style.EMPTY));
    }

    /**
     * Checks if the currently set language uses right to left writing.
     */
    public boolean isRightToLeft() {
        return Language.getInstance().isRightToLeft();
    }

    public TextHandler getTextHandler() {
        return this.handler;
    }

    @Environment(value=EnvType.CLIENT)
    class Drawer
    implements CharacterVisitor {
        final VertexConsumerProvider vertexConsumers;
        private final boolean shadow;
        private final float brightnessMultiplier;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;
        private final Matrix4f matrix;
        private final TextLayerType layerType;
        private final int light;
        float x;
        float y;
        @Nullable
        private List<GlyphRenderer.Rectangle> rectangles;

        private void addRectangle(GlyphRenderer.Rectangle rectangle) {
            if (this.rectangles == null) {
                this.rectangles = Lists.newArrayList();
            }
            this.rectangles.add(rectangle);
        }

        public Drawer(VertexConsumerProvider vertexConsumers, float x, float y, int color, boolean shadow, Matrix4f matrix, boolean seeThrough, int light) {
            this(vertexConsumers, x, y, color, shadow, matrix, seeThrough ? TextLayerType.SEE_THROUGH : TextLayerType.NORMAL, light);
        }

        public Drawer(VertexConsumerProvider vertexConsumers, float x, float y, int color, boolean shadow, Matrix4f matrix, TextLayerType layerType, int light) {
            this.vertexConsumers = vertexConsumers;
            this.x = x;
            this.y = y;
            this.shadow = shadow;
            this.brightnessMultiplier = shadow ? 0.25f : 1.0f;
            this.red = (float)(color >> 16 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.green = (float)(color >> 8 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.blue = (float)(color & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.alpha = (float)(color >> 24 & 0xFF) / 255.0f;
            this.matrix = matrix;
            this.layerType = layerType;
            this.light = light;
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            float m;
            float l;
            float h;
            float g;
            FontStorage fontStorage = TextRenderer.this.getFontStorage(style.getFont());
            Glyph glyph = fontStorage.getGlyph(j);
            GlyphRenderer glyphRenderer = style.isObfuscated() && j != 32 ? fontStorage.getObfuscatedGlyphRenderer(glyph) : fontStorage.getGlyphRenderer(j);
            boolean bl = style.isBold();
            float f = this.alpha;
            TextColor textColor = style.getColor();
            if (textColor != null) {
                int k = textColor.getRgb();
                g = (float)(k >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF)) / 255.0f * this.brightnessMultiplier;
                h = (float)(k >> 8 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF)) / 255.0f * this.brightnessMultiplier;
                l = (float)(k & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF)) / 255.0f * this.brightnessMultiplier;
            } else {
                g = this.red;
                h = this.green;
                l = this.blue;
            }
            if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
                float k = bl ? glyph.getBoldOffset() : 0.0f;
                m = this.shadow ? glyph.getShadowOffset() : 0.0f;
                VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));
                TextRenderer.this.drawGlyph(glyphRenderer, bl, style.isItalic(), k, this.x + m, this.y + m, this.matrix, vertexConsumer, g, h, l, f, this.light);
            }
            float k = glyph.getAdvance(bl);
            float f2 = m = this.shadow ? 1.0f : 0.0f;
            if (style.isStrikethrough()) {
                this.addRectangle(new GlyphRenderer.Rectangle(this.x + m - 1.0f, this.y + m + 4.5f, this.x + m + k, this.y + m + 4.5f - 1.0f, 0.01f, g, h, l, f));
            }
            if (style.isUnderlined()) {
                this.addRectangle(new GlyphRenderer.Rectangle(this.x + m - 1.0f, this.y + m + 9.0f, this.x + m + k, this.y + m + 9.0f - 1.0f, 0.01f, g, h, l, f));
            }
            this.x += k;
            return true;
        }

        public float drawLayer(int underlineColor, float x) {
            if (underlineColor != 0) {
                float f = (float)(underlineColor >> 24 & 0xFF) / 255.0f;
                float g = (float)(underlineColor >> 16 & 0xFF) / 255.0f;
                float h = (float)(underlineColor >> 8 & 0xFF) / 255.0f;
                float i = (float)(underlineColor & 0xFF) / 255.0f;
                this.addRectangle(new GlyphRenderer.Rectangle(x - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f, 0.01f, g, h, i, f));
            }
            if (this.rectangles != null) {
                GlyphRenderer f = TextRenderer.this.getFontStorage(Style.DEFAULT_FONT_ID).getRectangleRenderer();
                VertexConsumer g = this.vertexConsumers.getBuffer(f.getLayer(this.layerType));
                for (GlyphRenderer.Rectangle i : this.rectangles) {
                    f.drawRectangle(i, this.matrix, g, this.light);
                }
            }
            return this.x;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum TextLayerType {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;

    }
}

