/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class EmptyGlyphRenderer
extends GlyphRenderer {
    public EmptyGlyphRenderer() {
        super(RenderLayer.getText(new Identifier("")), RenderLayer.getTextSeeThrough(new Identifier("")), RenderLayer.getTextPolygonOffset(new Identifier("")), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void draw(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
    }
}

