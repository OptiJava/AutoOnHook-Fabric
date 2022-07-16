/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class BlockOutlineDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;

    public BlockOutlineDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        World blockView = this.client.player.world;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BlockPos blockPos = new BlockPos(cameraX, cameraY, cameraZ);
        for (BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-6, -6, -6), blockPos.add(6, 6, 6))) {
            BlockState blockState = blockView.getBlockState(blockPos2);
            if (blockState.isOf(Blocks.AIR)) continue;
            VoxelShape voxelShape = blockState.getOutlineShape(blockView, blockPos2);
            for (Box box : voxelShape.getBoundingBoxes()) {
                BufferBuilder bufferBuilder;
                Tessellator tessellator;
                Box box2 = box.offset(blockPos2).expand(0.002).offset(-cameraX, -cameraY, -cameraZ);
                double d = box2.minX;
                double e = box2.minY;
                double f = box2.minZ;
                double g = box2.maxX;
                double h = box2.maxY;
                double i = box2.maxZ;
                float j = 1.0f;
                float k = 0.0f;
                float l = 0.0f;
                float m = 0.5f;
                if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.WEST)) {
                    tessellator = Tessellator.getInstance();
                    bufferBuilder = tessellator.getBuffer();
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    bufferBuilder.vertex(d, e, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(d, e, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(d, h, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(d, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    tessellator.draw();
                }
                if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.SOUTH)) {
                    tessellator = Tessellator.getInstance();
                    bufferBuilder = tessellator.getBuffer();
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    bufferBuilder.vertex(d, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(d, e, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, e, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    tessellator.draw();
                }
                if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.EAST)) {
                    tessellator = Tessellator.getInstance();
                    bufferBuilder = tessellator.getBuffer();
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    bufferBuilder.vertex(g, e, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, e, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, h, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    tessellator.draw();
                }
                if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.NORTH)) {
                    tessellator = Tessellator.getInstance();
                    bufferBuilder = tessellator.getBuffer();
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    bufferBuilder.vertex(g, h, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, e, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(d, h, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(d, e, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    tessellator.draw();
                }
                if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.DOWN)) {
                    tessellator = Tessellator.getInstance();
                    bufferBuilder = tessellator.getBuffer();
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    bufferBuilder.vertex(d, e, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, e, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(d, e, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    bufferBuilder.vertex(g, e, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                    tessellator.draw();
                }
                if (!blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.UP)) continue;
                tessellator = Tessellator.getInstance();
                bufferBuilder = tessellator.getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(d, h, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                bufferBuilder.vertex(d, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                bufferBuilder.vertex(g, h, f).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                bufferBuilder.vertex(g, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).next();
                tessellator.draw();
            }
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}

