/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BufferRenderer {
    private static int currentVertexArray;
    private static int currentVertexBuffer;
    private static int currentElementBuffer;
    @Nullable
    private static VertexFormat vertexFormat;

    public static void unbindAll() {
        if (vertexFormat != null) {
            vertexFormat.endDrawing();
            vertexFormat = null;
        }
        GlStateManager._glBindBuffer(34963, 0);
        currentElementBuffer = 0;
        GlStateManager._glBindBuffer(34962, 0);
        currentVertexBuffer = 0;
        GlStateManager._glBindVertexArray(0);
        currentVertexArray = 0;
    }

    public static void unbindElementBuffer() {
        GlStateManager._glBindBuffer(34963, 0);
        currentElementBuffer = 0;
    }

    public static void draw(BufferBuilder bufferBuilder) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                Pair<BufferBuilder.DrawArrayParameters, ByteBuffer> pair = bufferBuilder.popData();
                BufferBuilder.DrawArrayParameters drawArrayParameters = pair.getFirst();
                BufferRenderer.draw(pair.getSecond(), drawArrayParameters.getMode(), drawArrayParameters.getVertexFormat(), drawArrayParameters.getCount(), drawArrayParameters.getElementFormat(), drawArrayParameters.getVertexCount(), drawArrayParameters.isTextured());
            });
        } else {
            Pair<BufferBuilder.DrawArrayParameters, ByteBuffer> pair = bufferBuilder.popData();
            BufferBuilder.DrawArrayParameters drawArrayParameters = pair.getFirst();
            BufferRenderer.draw(pair.getSecond(), drawArrayParameters.getMode(), drawArrayParameters.getVertexFormat(), drawArrayParameters.getCount(), drawArrayParameters.getElementFormat(), drawArrayParameters.getVertexCount(), drawArrayParameters.isTextured());
        }
    }

    private static void draw(ByteBuffer buffer, VertexFormat.DrawMode drawMode, VertexFormat vertexFormat, int count, VertexFormat.IntType elementFormat, int vertexCount, boolean textured) {
        int k;
        int j;
        Object indexBuffer;
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        buffer.clear();
        if (count <= 0) {
            return;
        }
        int i = count * vertexFormat.getVertexSize();
        BufferRenderer.bind(vertexFormat);
        buffer.position(0);
        buffer.limit(i);
        GlStateManager._glBufferData(34962, buffer, 35048);
        if (textured) {
            indexBuffer = RenderSystem.getSequentialBuffer(drawMode, vertexCount);
            j = ((RenderSystem.IndexBuffer)indexBuffer).getId();
            if (j != currentElementBuffer) {
                GlStateManager._glBindBuffer(34963, j);
                currentElementBuffer = j;
            }
            k = ((RenderSystem.IndexBuffer)indexBuffer).getElementFormat().count;
        } else {
            int indexBuffer2 = vertexFormat.getElementBuffer();
            if (indexBuffer2 != currentElementBuffer) {
                GlStateManager._glBindBuffer(34963, indexBuffer2);
                currentElementBuffer = indexBuffer2;
            }
            buffer.position(i);
            buffer.limit(i + vertexCount * elementFormat.size);
            GlStateManager._glBufferData(34963, buffer, 35048);
            k = elementFormat.count;
        }
        indexBuffer = RenderSystem.getShader();
        for (j = 0; j < 8; ++j) {
            int l = RenderSystem.getShaderTexture(j);
            ((Shader)indexBuffer).addSampler("Sampler" + j, l);
        }
        if (((Shader)indexBuffer).modelViewMat != null) {
            ((Shader)indexBuffer).modelViewMat.set(RenderSystem.getModelViewMatrix());
        }
        if (((Shader)indexBuffer).projectionMat != null) {
            ((Shader)indexBuffer).projectionMat.set(RenderSystem.getProjectionMatrix());
        }
        if (((Shader)indexBuffer).colorModulator != null) {
            ((Shader)indexBuffer).colorModulator.set(RenderSystem.getShaderColor());
        }
        if (((Shader)indexBuffer).fogStart != null) {
            ((Shader)indexBuffer).fogStart.set(RenderSystem.getShaderFogStart());
        }
        if (((Shader)indexBuffer).fogEnd != null) {
            ((Shader)indexBuffer).fogEnd.set(RenderSystem.getShaderFogEnd());
        }
        if (((Shader)indexBuffer).fogColor != null) {
            ((Shader)indexBuffer).fogColor.set(RenderSystem.getShaderFogColor());
        }
        if (((Shader)indexBuffer).textureMat != null) {
            ((Shader)indexBuffer).textureMat.set(RenderSystem.getTextureMatrix());
        }
        if (((Shader)indexBuffer).gameTime != null) {
            ((Shader)indexBuffer).gameTime.set(RenderSystem.getShaderGameTime());
        }
        if (((Shader)indexBuffer).screenSize != null) {
            Window j2 = MinecraftClient.getInstance().getWindow();
            ((Shader)indexBuffer).screenSize.set((float)j2.getFramebufferWidth(), (float)j2.getFramebufferHeight());
        }
        if (((Shader)indexBuffer).lineWidth != null && (drawMode == VertexFormat.DrawMode.LINES || drawMode == VertexFormat.DrawMode.LINE_STRIP)) {
            ((Shader)indexBuffer).lineWidth.set(RenderSystem.getShaderLineWidth());
        }
        RenderSystem.setupShaderLights((Shader)indexBuffer);
        ((Shader)indexBuffer).bind();
        GlStateManager._drawElements(drawMode.mode, vertexCount, k, 0L);
        ((Shader)indexBuffer).unbind();
        buffer.position(0);
    }

    /**
     * Similar to a regular draw, however this method will skip rendering shaders.
     */
    public static void postDraw(BufferBuilder builder) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Pair<BufferBuilder.DrawArrayParameters, ByteBuffer> pair = builder.popData();
        BufferBuilder.DrawArrayParameters drawArrayParameters = pair.getFirst();
        ByteBuffer byteBuffer = pair.getSecond();
        VertexFormat vertexFormat = drawArrayParameters.getVertexFormat();
        int i = drawArrayParameters.getCount();
        byteBuffer.clear();
        if (i <= 0) {
            return;
        }
        int j = i * vertexFormat.getVertexSize();
        BufferRenderer.bind(vertexFormat);
        byteBuffer.position(0);
        byteBuffer.limit(j);
        GlStateManager._glBufferData(34962, byteBuffer, 35048);
        RenderSystem.IndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(drawArrayParameters.getMode(), drawArrayParameters.getVertexCount());
        int k = indexBuffer.getId();
        if (k != currentElementBuffer) {
            GlStateManager._glBindBuffer(34963, k);
            currentElementBuffer = k;
        }
        int l = indexBuffer.getElementFormat().count;
        GlStateManager._drawElements(drawArrayParameters.getMode().mode, drawArrayParameters.getVertexCount(), l, 0L);
        byteBuffer.position(0);
    }

    private static void bind(VertexFormat vertexFormat) {
        boolean bl;
        int i = vertexFormat.getVertexArray();
        int j = vertexFormat.getVertexBuffer();
        boolean bl2 = bl = vertexFormat != BufferRenderer.vertexFormat;
        if (bl) {
            BufferRenderer.unbindAll();
        }
        if (i != currentVertexArray) {
            GlStateManager._glBindVertexArray(i);
            currentVertexArray = i;
        }
        if (j != currentVertexBuffer) {
            GlStateManager._glBindBuffer(34962, j);
            currentVertexBuffer = j;
        }
        if (bl) {
            vertexFormat.startDrawing();
            BufferRenderer.vertexFormat = vertexFormat;
        }
    }
}

