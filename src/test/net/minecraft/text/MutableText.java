/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.text;

import java.util.function.UnaryOperator;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * A text with mutation operations.
 */
public interface MutableText
extends Text {
    /**
     * Sets the style of this text.
     */
    public MutableText setStyle(Style var1);

    /**
     * Appends a literal text with content {@code text} to this text's siblings.
     * 
     * @param text the literal text content
     */
    default public MutableText append(String text) {
        return this.append(new LiteralText(text));
    }

    /**
     * Appends a text to this text's siblings.
     * 
     * @param text the sibling
     */
    public MutableText append(Text var1);

    /**
     * Updates the style of this text.
     * 
     * @see Text#getStyle()
     * @see #setStyle(Style)
     * 
     * @param styleUpdater the style updater
     */
    default public MutableText styled(UnaryOperator<Style> styleUpdater) {
        this.setStyle((Style)styleUpdater.apply(this.getStyle()));
        return this;
    }

    /**
     * Fills the absent parts of this text's style with definitions from {@code
     * styleOverride}.
     * 
     * @see Style#withParent(Style)
     * 
     * @param styleOverride the style that provides definitions for absent definitions in this text's style
     */
    default public MutableText fillStyle(Style styleOverride) {
        this.setStyle(styleOverride.withParent(this.getStyle()));
        return this;
    }

    /**
     * Adds some formattings to this text's style.
     * 
     * @param formattings an array of formattings
     */
    default public MutableText formatted(Formatting ... formattings) {
        this.setStyle(this.getStyle().withFormatting(formattings));
        return this;
    }

    /**
     * Add a formatting to this text's style.
     * 
     * @param formatting a formatting
     */
    default public MutableText formatted(Formatting formatting) {
        this.setStyle(this.getStyle().withFormatting(formatting));
        return this;
    }
}

