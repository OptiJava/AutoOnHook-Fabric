/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;

@Environment(value=EnvType.CLIENT)
public class ChunkBorderDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;

    public ChunkBorderDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        int i;
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Entity entity = this.client.gameRenderer.getCamera().getFocusedEntity();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        double d = (double)this.client.world.getBottomY() - cameraY;
        double e = (double)this.client.world.getTopY() - cameraY;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        ChunkPos chunkPos = entity.getChunkPos();
        double f = (double)chunkPos.getStartX() - cameraX;
        double g = (double)chunkPos.getStartZ() - cameraZ;
        RenderSystem.lineWidth(1.0f);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (i = -16; i <= 32; i += 16) {
            for (int j = -16; j <= 32; j += 16) {
                bufferBuilder.vertex(f + (double)i, d, g + (double)j).color(1.0f, 0.0f, 0.0f, 0.0f).next();
                bufferBuilder.vertex(f + (double)i, d, g + (double)j).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                bufferBuilder.vertex(f + (double)i, e, g + (double)j).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                bufferBuilder.vertex(f + (double)i, e, g + (double)j).color(1.0f, 0.0f, 0.0f, 0.0f).next();
            }
        }
        for (i = 2; i < 16; i += 2) {
            bufferBuilder.vertex(f + (double)i, d, g).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(f + (double)i, d, g).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + (double)i, e, g).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + (double)i, e, g).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(f + (double)i, d, g + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(f + (double)i, d, g + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + (double)i, e, g + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + (double)i, e, g + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).next();
        }
        for (i = 2; i < 16; i += 2) {
            bufferBuilder.vertex(f, d, g + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(f, d, g + (double)i).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f, e, g + (double)i).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f, e, g + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(f + 16.0, d, g + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(f + 16.0, d, g + (double)i).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + 16.0, e, g + (double)i).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + 16.0, e, g + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
        }
        for (i = this.client.world.getBottomY(); i <= this.client.world.getTopY(); i += 2) {
            double j = (double)i - cameraY;
            bufferBuilder.vertex(f, j, g).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(f, j, g).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f, j, g + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + 16.0, j, g + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f + 16.0, j, g).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f, j, g).color(1.0f, 1.0f, 0.0f, 1.0f).next();
            bufferBuilder.vertex(f, j, g).color(1.0f, 1.0f, 0.0f, 0.0f).next();
        }
        tessellator.draw();
        RenderSystem.lineWidth(2.0f);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (i = 0; i <= 16; i += 16) {
            for (int j = 0; j <= 16; j += 16) {
                bufferBuilder.vertex(f + (double)i, d, g + (double)j).color(0.25f, 0.25f, 1.0f, 0.0f).next();
                bufferBuilder.vertex(f + (double)i, d, g + (double)j).color(0.25f, 0.25f, 1.0f, 1.0f).next();
                bufferBuilder.vertex(f + (double)i, e, g + (double)j).color(0.25f, 0.25f, 1.0f, 1.0f).next();
                bufferBuilder.vertex(f + (double)i, e, g + (double)j).color(0.25f, 0.25f, 1.0f, 0.0f).next();
            }
        }
        for (i = this.client.world.getBottomY(); i <= this.client.world.getTopY(); i += 16) {
            double j = (double)i - cameraY;
            bufferBuilder.vertex(f, j, g).color(0.25f, 0.25f, 1.0f, 0.0f).next();
            bufferBuilder.vertex(f, j, g).color(0.25f, 0.25f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(f, j, g + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(f + 16.0, j, g + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(f + 16.0, j, g).color(0.25f, 0.25f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(f, j, g).color(0.25f, 0.25f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(f, j, g).color(0.25f, 0.25f, 1.0f, 0.0f).next();
        }
        tessellator.draw();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }
}
