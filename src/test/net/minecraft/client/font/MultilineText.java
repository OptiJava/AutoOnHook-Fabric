/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public interface MultilineText {
    public static final MultilineText EMPTY = new MultilineText(){

        @Override
        public int drawCenterWithShadow(MatrixStack matrices, int x, int y) {
            return y;
        }

        @Override
        public int drawCenterWithShadow(MatrixStack matrices, int x, int y, int lineHeight, int color) {
            return y;
        }

        @Override
        public int drawWithShadow(MatrixStack matrices, int x, int y, int lineHeight, int color) {
            return y;
        }

        @Override
        public int draw(MatrixStack matrices, int x, int y, int lineHeight, int color) {
            return y;
        }

        @Override
        public int count() {
            return 0;
        }
    };

    public static MultilineText create(TextRenderer renderer, StringVisitable text2, int width) {
        return MultilineText.create(renderer, renderer.wrapLines(text2, width).stream().map(text -> new Line((OrderedText)text, renderer.getWidth((OrderedText)text))).collect(ImmutableList.toImmutableList()));
    }

    public static MultilineText create(TextRenderer renderer, StringVisitable text2, int width, int maxLines) {
        return MultilineText.create(renderer, renderer.wrapLines(text2, width).stream().limit(maxLines).map(text -> new Line((OrderedText)text, renderer.getWidth((OrderedText)text))).collect(ImmutableList.toImmutableList()));
    }

    public static MultilineText create(TextRenderer renderer, Text ... texts) {
        return MultilineText.create(renderer, Arrays.stream(texts).map(Text::asOrderedText).map(text -> new Line((OrderedText)text, renderer.getWidth((OrderedText)text))).collect(ImmutableList.toImmutableList()));
    }

    public static MultilineText method_35726(TextRenderer renderer, List<Text> texts) {
        return MultilineText.create(renderer, texts.stream().map(Text::asOrderedText).map(text -> new Line((OrderedText)text, renderer.getWidth((OrderedText)text))).collect(ImmutableList.toImmutableList()));
    }

    public static MultilineText create(final TextRenderer renderer, final List<Line> lines) {
        if (lines.isEmpty()) {
            return EMPTY;
        }
        return new MultilineText(){

            @Override
            public int drawCenterWithShadow(MatrixStack matrices, int x, int y) {
                return this.drawCenterWithShadow(matrices, x, y, renderer.fontHeight, 0xFFFFFF);
            }

            @Override
            public int drawCenterWithShadow(MatrixStack matrices, int x, int y, int lineHeight, int color) {
                int i = y;
                for (Line line : lines) {
                    renderer.drawWithShadow(matrices, line.text, (float)(x - line.width / 2), (float)i, color);
                    i += lineHeight;
                }
                return i;
            }

            @Override
            public int drawWithShadow(MatrixStack matrices, int x, int y, int lineHeight, int color) {
                int i = y;
                for (Line line : lines) {
                    renderer.drawWithShadow(matrices, line.text, (float)x, (float)i, color);
                    i += lineHeight;
                }
                return i;
            }

            @Override
            public int draw(MatrixStack matrices, int x, int y, int lineHeight, int color) {
                int i = y;
                for (Line line : lines) {
                    renderer.draw(matrices, line.text, (float)x, (float)i, color);
                    i += lineHeight;
                }
                return i;
            }

            @Override
            public int count() {
                return lines.size();
            }
        };
    }

    public int drawCenterWithShadow(MatrixStack var1, int var2, int var3);

    public int drawCenterWithShadow(MatrixStack var1, int var2, int var3, int var4, int var5);

    public int drawWithShadow(MatrixStack var1, int var2, int var3, int var4, int var5);

    public int draw(MatrixStack var1, int var2, int var3, int var4, int var5);

    public int count();

    @Environment(value=EnvType.CLIENT)
    public static class Line {
        final OrderedText text;
        final int width;

        Line(OrderedText text, int width) {
            this.text = text;
            this.width = width;
        }
    }
}

