/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextHandler {
    final WidthRetriever widthRetriever;

    public TextHandler(WidthRetriever widthRetriever) {
        this.widthRetriever = widthRetriever;
    }

    /**
     * {@return the width of a string}
     */
    public float getWidth(@Nullable String text) {
        if (text == null) {
            return 0.0f;
        }
        MutableFloat mutableFloat = new MutableFloat();
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (unused, style, codePoint) -> {
            mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    /**
     * {@return the width of a text}
     */
    public float getWidth(StringVisitable text) {
        MutableFloat mutableFloat = new MutableFloat();
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (unused, style, codePoint) -> {
            mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    public float getWidth(OrderedText text) {
        MutableFloat mutableFloat = new MutableFloat();
        text.accept((index, style, codePoint) -> {
            mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    /**
     * {@return the length of a string when it is trimmed to be at most {@code
     * maxWidth} wide}
     * 
     * @param text the string to trim
     * @param maxWidth the max width of the trimmed string
     * @param style the style of the trimmed string
     */
    public int getTrimmedLength(String text, int maxWidth, Style style) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(maxWidth);
        TextVisitFactory.visitForwards(text, style, widthLimitingVisitor);
        return widthLimitingVisitor.getLength();
    }

    /**
     * Trim a string to be at most {@code maxWidth} wide.
     * 
     * @return the trimmed string
     * 
     * @param text the string to trim
     * @param maxWidth the max width of the trimmed string
     * @param style the style of the trimmed string
     */
    public String trimToWidth(String text, int maxWidth, Style style) {
        return text.substring(0, this.getTrimmedLength(text, maxWidth, style));
    }

    /**
     * Trim a string from right to left to be at most {@code maxWidth} wide.
     * 
     * @return the trimmed string
     * 
     * @param text the string to trim
     * @param maxWidth the max width of the trimmed string
     * @param style the style of the trimmed string
     */
    public String trimToWidthBackwards(String text, int maxWidth, Style style2) {
        MutableFloat mutableFloat = new MutableFloat();
        MutableInt mutableInt = new MutableInt(text.length());
        TextVisitFactory.visitBackwards(text, style2, (index, style, codePoint) -> {
            float f = mutableFloat.addAndGet(this.widthRetriever.getWidth(codePoint, style));
            if (f > (float)maxWidth) {
                return false;
            }
            mutableInt.setValue(index);
            return true;
        });
        return text.substring(mutableInt.intValue());
    }

    public int getLimitedStringLength(String text, int maxWidth, Style style) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(maxWidth);
        TextVisitFactory.visitFormatted(text, style, (CharacterVisitor)widthLimitingVisitor);
        return widthLimitingVisitor.getLength();
    }

    /**
     * {@return the style at the {@code x} position in a text}
     * 
     * @param x the position in the text
     */
    @Nullable
    public Style getStyleAt(StringVisitable text2, int x) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(x);
        return text2.visit((style, text) -> TextVisitFactory.visitFormatted(text, style, (CharacterVisitor)widthLimitingVisitor) ? Optional.empty() : Optional.of(style), Style.EMPTY).orElse(null);
    }

    /**
     * Gets the style at the {@code x} position in a text.
     * 
     * @return a style
     * 
     * @param x the position in the text
     */
    @Nullable
    public Style getStyleAt(OrderedText text, int x) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(x);
        MutableObject mutableObject = new MutableObject();
        text.accept((index, style, codePoint) -> {
            if (!widthLimitingVisitor.accept(index, style, codePoint)) {
                mutableObject.setValue(style);
                return false;
            }
            return true;
        });
        return (Style)mutableObject.getValue();
    }

    public String limitString(String text, int maxWidth, Style style) {
        return text.substring(0, this.getLimitedStringLength(text, maxWidth, style));
    }

    public StringVisitable trimToWidth(StringVisitable text, int width, Style style) {
        final WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(width);
        return text.visit(new StringVisitable.StyledVisitor<StringVisitable>(){
            private final TextCollector collector = new TextCollector();

            @Override
            public Optional<StringVisitable> accept(Style style, String string) {
                widthLimitingVisitor.resetLength();
                if (!TextVisitFactory.visitFormatted(string, style, (CharacterVisitor)widthLimitingVisitor)) {
                    String string2 = string.substring(0, widthLimitingVisitor.getLength());
                    if (!string2.isEmpty()) {
                        this.collector.add(StringVisitable.styled(string2, style));
                    }
                    return Optional.of(this.collector.getCombined());
                }
                if (!string.isEmpty()) {
                    this.collector.add(StringVisitable.styled(string, style));
                }
                return Optional.empty();
            }
        }, style).orElse(text);
    }

    public int method_35717(String text, int maxWidth, Style style) {
        LineBreakingVisitor lineBreakingVisitor = new LineBreakingVisitor(maxWidth);
        TextVisitFactory.visitFormatted(text, style, (CharacterVisitor)lineBreakingVisitor);
        return lineBreakingVisitor.getEndingIndex();
    }

    public static int moveCursorByWords(String text, int offset, int cursor, boolean consumeSpaceOrBreak) {
        int i = cursor;
        boolean bl = offset < 0;
        int j = Math.abs(offset);
        for (int k = 0; k < j; ++k) {
            if (bl) {
                while (consumeSpaceOrBreak && i > 0 && (text.charAt(i - 1) == ' ' || text.charAt(i - 1) == '\n')) {
                    --i;
                }
                while (i > 0 && text.charAt(i - 1) != ' ' && text.charAt(i - 1) != '\n') {
                    --i;
                }
                continue;
            }
            int l = text.length();
            int m = text.indexOf(32, i);
            int n = text.indexOf(10, i);
            i = m == -1 && n == -1 ? -1 : (m != -1 && n != -1 ? Math.min(m, n) : (m != -1 ? m : n));
            if (i == -1) {
                i = l;
                continue;
            }
            while (consumeSpaceOrBreak && i < l && (text.charAt(i) == ' ' || text.charAt(i) == '\n')) {
                ++i;
            }
        }
        return i;
    }

    public void wrapLines(String text, int maxWidth, Style style, boolean retainTrailingWordSplit, LineWrappingConsumer consumer) {
        int i = 0;
        int j = text.length();
        Style style2 = style;
        while (i < j) {
            LineBreakingVisitor lineBreakingVisitor = new LineBreakingVisitor(maxWidth);
            boolean bl = TextVisitFactory.visitFormatted(text, i, style2, style, lineBreakingVisitor);
            if (bl) {
                consumer.accept(style2, i, j);
                break;
            }
            int k = lineBreakingVisitor.getEndingIndex();
            char c = text.charAt(k);
            int l = c == '\n' || c == ' ' ? k + 1 : k;
            consumer.accept(style2, i, retainTrailingWordSplit ? l : k);
            i = l;
            style2 = lineBreakingVisitor.getEndingStyle();
        }
    }

    public List<StringVisitable> wrapLines(String text, int maxWidth, Style style2) {
        ArrayList<StringVisitable> list = Lists.newArrayList();
        this.wrapLines(text, maxWidth, style2, false, (style, start, end) -> list.add(StringVisitable.styled(text.substring(start, end), style)));
        return list;
    }

    public List<StringVisitable> wrapLines(StringVisitable text2, int maxWidth, Style style) {
        ArrayList<StringVisitable> list = Lists.newArrayList();
        this.wrapLines(text2, maxWidth, style, (text, boolean_) -> list.add((StringVisitable)text));
        return list;
    }

    public List<StringVisitable> method_35714(StringVisitable stringVisitable, int maxWidth, Style style, StringVisitable stringVisitable22) {
        ArrayList<StringVisitable> list = Lists.newArrayList();
        this.wrapLines(stringVisitable, maxWidth, style, (stringVisitable2, boolean_) -> list.add(boolean_ != false ? StringVisitable.concat(stringVisitable22, stringVisitable2) : stringVisitable2));
        return list;
    }

    public void wrapLines(StringVisitable text2, int maxWidth, Style style2, BiConsumer<StringVisitable, Boolean> biConsumer) {
        Object lineBreakingVisitor;
        ArrayList<StyledString> list = Lists.newArrayList();
        text2.visit((style, text) -> {
            if (!text.isEmpty()) {
                list.add(new StyledString(text, style));
            }
            return Optional.empty();
        }, style2);
        LineWrappingCollector lineWrappingCollector = new LineWrappingCollector(list);
        boolean bl = true;
        boolean bl2 = false;
        boolean bl3 = false;
        block0: while (bl) {
            bl = false;
            lineBreakingVisitor = new LineBreakingVisitor(maxWidth);
            for (StyledString styledString : lineWrappingCollector.parts) {
                boolean bl4 = TextVisitFactory.visitFormatted(styledString.literal, 0, styledString.style, style2, (CharacterVisitor)lineBreakingVisitor);
                if (!bl4) {
                    int i = ((LineBreakingVisitor)lineBreakingVisitor).getEndingIndex();
                    Style style22 = ((LineBreakingVisitor)lineBreakingVisitor).getEndingStyle();
                    char c = lineWrappingCollector.charAt(i);
                    boolean bl5 = c == '\n';
                    boolean bl6 = bl5 || c == ' ';
                    bl2 = bl5;
                    StringVisitable stringVisitable = lineWrappingCollector.collectLine(i, bl6 ? 1 : 0, style22);
                    biConsumer.accept(stringVisitable, bl3);
                    bl3 = !bl5;
                    bl = true;
                    continue block0;
                }
                ((LineBreakingVisitor)lineBreakingVisitor).offset(styledString.literal.length());
            }
        }
        lineBreakingVisitor = lineWrappingCollector.collectRemainers();
        if (lineBreakingVisitor != null) {
            biConsumer.accept((StringVisitable)lineBreakingVisitor, bl3);
        } else if (bl2) {
            biConsumer.accept(StringVisitable.EMPTY, false);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface WidthRetriever {
        public float getWidth(int var1, Style var2);
    }

    @Environment(value=EnvType.CLIENT)
    class WidthLimitingVisitor
    implements CharacterVisitor {
        private float widthLeft;
        private int length;

        public WidthLimitingVisitor(float maxWidth) {
            this.widthLeft = maxWidth;
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            this.widthLeft -= TextHandler.this.widthRetriever.getWidth(j, style);
            if (this.widthLeft >= 0.0f) {
                this.length = i + Character.charCount(j);
                return true;
            }
            return false;
        }

        public int getLength() {
            return this.length;
        }

        public void resetLength() {
            this.length = 0;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class LineBreakingVisitor
    implements CharacterVisitor {
        private final float maxWidth;
        private int endIndex = -1;
        private Style endStyle = Style.EMPTY;
        private boolean nonEmpty;
        private float totalWidth;
        private int lastSpaceBreak = -1;
        private Style lastSpaceStyle = Style.EMPTY;
        private int count;
        private int startOffset;

        public LineBreakingVisitor(float maxWidth) {
            this.maxWidth = Math.max(maxWidth, 1.0f);
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            int k = i + this.startOffset;
            switch (j) {
                case 10: {
                    return this.breakLine(k, style);
                }
                case 32: {
                    this.lastSpaceBreak = k;
                    this.lastSpaceStyle = style;
                }
            }
            float f = TextHandler.this.widthRetriever.getWidth(j, style);
            this.totalWidth += f;
            if (this.nonEmpty && this.totalWidth > this.maxWidth) {
                if (this.lastSpaceBreak != -1) {
                    return this.breakLine(this.lastSpaceBreak, this.lastSpaceStyle);
                }
                return this.breakLine(k, style);
            }
            this.nonEmpty |= f != 0.0f;
            this.count = k + Character.charCount(j);
            return true;
        }

        private boolean breakLine(int finishIndex, Style finishStyle) {
            this.endIndex = finishIndex;
            this.endStyle = finishStyle;
            return false;
        }

        private boolean hasLineBreak() {
            return this.endIndex != -1;
        }

        public int getEndingIndex() {
            return this.hasLineBreak() ? this.endIndex : this.count;
        }

        public Style getEndingStyle() {
            return this.endStyle;
        }

        public void offset(int extraOffset) {
            this.startOffset += extraOffset;
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface LineWrappingConsumer {
        public void accept(Style var1, int var2, int var3);
    }

    @Environment(value=EnvType.CLIENT)
    static class LineWrappingCollector {
        final List<StyledString> parts;
        private String joined;

        public LineWrappingCollector(List<StyledString> parts) {
            this.parts = parts;
            this.joined = parts.stream().map(styledString -> styledString.literal).collect(Collectors.joining());
        }

        public char charAt(int index) {
            return this.joined.charAt(index);
        }

        public StringVisitable collectLine(int lineLength, int skippedLength, Style style) {
            TextCollector textCollector = new TextCollector();
            ListIterator<StyledString> listIterator = this.parts.listIterator();
            int i = lineLength;
            boolean bl = false;
            while (listIterator.hasNext()) {
                String string2;
                StyledString styledString = listIterator.next();
                String string = styledString.literal;
                int j = string.length();
                if (!bl) {
                    if (i > j) {
                        textCollector.add(styledString);
                        listIterator.remove();
                        i -= j;
                    } else {
                        string2 = string.substring(0, i);
                        if (!string2.isEmpty()) {
                            textCollector.add(StringVisitable.styled(string2, styledString.style));
                        }
                        i += skippedLength;
                        bl = true;
                    }
                }
                if (!bl) continue;
                if (i > j) {
                    listIterator.remove();
                    i -= j;
                    continue;
                }
                string2 = string.substring(i);
                if (string2.isEmpty()) {
                    listIterator.remove();
                    break;
                }
                listIterator.set(new StyledString(string2, style));
                break;
            }
            this.joined = this.joined.substring(lineLength + skippedLength);
            return textCollector.getCombined();
        }

        @Nullable
        public StringVisitable collectRemainers() {
            TextCollector textCollector = new TextCollector();
            this.parts.forEach(textCollector::add);
            this.parts.clear();
            return textCollector.getRawCombined();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class StyledString
    implements StringVisitable {
        final String literal;
        final Style style;

        public StyledString(String literal, Style style) {
            this.literal = literal;
            this.style = style;
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(this.literal);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
            return styledVisitor.accept(this.style.withParent(style), this.literal);
        }
    }
}

