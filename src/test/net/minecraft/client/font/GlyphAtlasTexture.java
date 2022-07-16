/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import com.mojang.blaze3d.platform.TextureUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlyphAtlasTexture
extends AbstractTexture {
    private static final int field_32227 = 256;
    private final Identifier id;
    private final RenderLayer textLayer;
    private final RenderLayer seeThroughTextLayer;
    private final RenderLayer polygonOffsetTextLayer;
    private final boolean hasColor;
    private final Slot rootSlot;

    public GlyphAtlasTexture(Identifier id, boolean hasColor) {
        this.id = id;
        this.hasColor = hasColor;
        this.rootSlot = new Slot(0, 0, 256, 256);
        TextureUtil.prepareImage(hasColor ? NativeImage.InternalFormat.RGBA : NativeImage.InternalFormat.RED, this.getGlId(), 256, 256);
        this.textLayer = hasColor ? RenderLayer.getText(id) : RenderLayer.getTextIntensity(id);
        this.seeThroughTextLayer = hasColor ? RenderLayer.getTextSeeThrough(id) : RenderLayer.getTextIntensitySeeThrough(id);
        this.polygonOffsetTextLayer = hasColor ? RenderLayer.getTextPolygonOffset(id) : RenderLayer.getTextIntensityPolygonOffset(id);
    }

    @Override
    public void load(ResourceManager manager) {
    }

    @Override
    public void close() {
        this.clearGlId();
    }

    @Nullable
    public GlyphRenderer getGlyphRenderer(RenderableGlyph glyph) {
        if (glyph.hasColor() != this.hasColor) {
            return null;
        }
        Slot slot = this.rootSlot.findSlotFor(glyph);
        if (slot != null) {
            this.bindTexture();
            glyph.upload(slot.x, slot.y);
            float f = 256.0f;
            float g = 256.0f;
            float h = 0.01f;
            return new GlyphRenderer(this.textLayer, this.seeThroughTextLayer, this.polygonOffsetTextLayer, ((float)slot.x + 0.01f) / 256.0f, ((float)slot.x - 0.01f + (float)glyph.getWidth()) / 256.0f, ((float)slot.y + 0.01f) / 256.0f, ((float)slot.y - 0.01f + (float)glyph.getHeight()) / 256.0f, glyph.getXMin(), glyph.getXMax(), glyph.getYMin(), glyph.getYMax());
        }
        return null;
    }

    public Identifier getId() {
        return this.id;
    }

    @Environment(value=EnvType.CLIENT)
    static class Slot {
        final int x;
        final int y;
        private final int width;
        private final int height;
        private Slot subSlot1;
        private Slot subSlot2;
        private boolean occupied;

        Slot(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Nullable
        Slot findSlotFor(RenderableGlyph glyph) {
            if (this.subSlot1 != null && this.subSlot2 != null) {
                Slot slot = this.subSlot1.findSlotFor(glyph);
                if (slot == null) {
                    slot = this.subSlot2.findSlotFor(glyph);
                }
                return slot;
            }
            if (this.occupied) {
                return null;
            }
            int slot = glyph.getWidth();
            int i = glyph.getHeight();
            if (slot > this.width || i > this.height) {
                return null;
            }
            if (slot == this.width && i == this.height) {
                this.occupied = true;
                return this;
            }
            int j = this.width - slot;
            int k = this.height - i;
            if (j > k) {
                this.subSlot1 = new Slot(this.x, this.y, slot, this.height);
                this.subSlot2 = new Slot(this.x + slot + 1, this.y, this.width - slot - 1, this.height);
            } else {
                this.subSlot1 = new Slot(this.x, this.y, this.width, i);
                this.subSlot2 = new Slot(this.x, this.y + i + 1, this.width, this.height - i - 1);
            }
            return this.subSlot1.findSlotFor(glyph);
        }
    }
}

