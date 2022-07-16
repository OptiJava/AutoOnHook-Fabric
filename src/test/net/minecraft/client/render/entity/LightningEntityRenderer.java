/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class LightningEntityRenderer
extends EntityRenderer<LightningEntity> {
    public LightningEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(LightningEntity lightningEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        float[] fs = new float[8];
        float[] gs = new float[8];
        float h = 0.0f;
        float j = 0.0f;
        Object random = new Random(lightningEntity.seed);
        for (int k = 7; k >= 0; --k) {
            fs[k] = h;
            gs[k] = j;
            h += (float)(((Random)random).nextInt(11) - 5);
            j += (float)(((Random)random).nextInt(11) - 5);
        }
        random = vertexConsumerProvider.getBuffer(RenderLayer.getLightning());
        Matrix4f k = matrixStack.peek().getModel();
        for (int l = 0; l < 4; ++l) {
            Random random2 = new Random(lightningEntity.seed);
            for (int m = 0; m < 3; ++m) {
                int n = 7;
                int o = 0;
                if (m > 0) {
                    n = 7 - m;
                }
                if (m > 0) {
                    o = n - 2;
                }
                float p = fs[n] - h;
                float q = gs[n] - j;
                for (int r = n; r >= o; --r) {
                    float s = p;
                    float t = q;
                    if (m == 0) {
                        p += (float)(random2.nextInt(11) - 5);
                        q += (float)(random2.nextInt(11) - 5);
                    } else {
                        p += (float)(random2.nextInt(31) - 15);
                        q += (float)(random2.nextInt(31) - 15);
                    }
                    float u = 0.5f;
                    float v = 0.45f;
                    float w = 0.45f;
                    float x = 0.5f;
                    float y = 0.1f + (float)l * 0.2f;
                    if (m == 0) {
                        y = (float)((double)y * ((double)r * 0.1 + 1.0));
                    }
                    float z = 0.1f + (float)l * 0.2f;
                    if (m == 0) {
                        z *= (float)(r - 1) * 0.1f + 1.0f;
                    }
                    LightningEntityRenderer.drawBranch(k, (VertexConsumer)random, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, false, false, true, false);
                    LightningEntityRenderer.drawBranch(k, (VertexConsumer)random, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, true, false, true, true);
                    LightningEntityRenderer.drawBranch(k, (VertexConsumer)random, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, true, true, false, true);
                    LightningEntityRenderer.drawBranch(k, (VertexConsumer)random, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, false, true, false, false);
                }
            }
        }
    }

    private static void drawBranch(Matrix4f matrix, VertexConsumer buffer, float x1, float z1, int y, float x2, float z2, float red, float green, float blue, float offset2, float offset1, boolean shiftEast1, boolean shiftSouth1, boolean shiftEast2, boolean shiftSouth2) {
        buffer.vertex(matrix, x1 + (shiftEast1 ? offset1 : -offset1), y * 16, z1 + (shiftSouth1 ? offset1 : -offset1)).color(red, green, blue, 0.3f).next();
        buffer.vertex(matrix, x2 + (shiftEast1 ? offset2 : -offset2), (y + 1) * 16, z2 + (shiftSouth1 ? offset2 : -offset2)).color(red, green, blue, 0.3f).next();
        buffer.vertex(matrix, x2 + (shiftEast2 ? offset2 : -offset2), (y + 1) * 16, z2 + (shiftSouth2 ? offset2 : -offset2)).color(red, green, blue, 0.3f).next();
        buffer.vertex(matrix, x1 + (shiftEast2 ? offset1 : -offset1), y * 16, z1 + (shiftSouth2 ? offset1 : -offset1)).color(red, green, blue, 0.3f).next();
    }

    @Override
    public Identifier getTexture(LightningEntity lightningEntity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}

