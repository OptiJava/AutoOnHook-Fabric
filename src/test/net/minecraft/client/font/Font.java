/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Closeable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.RenderableGlyph;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface Font
extends Closeable {
    @Override
    default public void close() {
    }

    @Nullable
    default public RenderableGlyph getGlyph(int codePoint) {
        return null;
    }

    /**
     * {@return the set of code points for which this font can provide glyphs}
     */
    public IntSet getProvidedGlyphs();
}

