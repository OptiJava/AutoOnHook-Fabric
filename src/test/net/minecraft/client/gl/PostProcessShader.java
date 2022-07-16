/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.JsonEffectGlShader;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class PostProcessShader
implements AutoCloseable {
    private final JsonEffectGlShader program;
    public final Framebuffer input;
    public final Framebuffer output;
    private final List<IntSupplier> samplerValues = Lists.newArrayList();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerWidths = Lists.newArrayList();
    private final List<Integer> samplerHeights = Lists.newArrayList();
    private Matrix4f projectionMatrix;

    public PostProcessShader(ResourceManager resourceManager, String programName, Framebuffer input, Framebuffer output) throws IOException {
        this.program = new JsonEffectGlShader(resourceManager, programName);
        this.input = input;
        this.output = output;
    }

    @Override
    public void close() {
        this.program.close();
    }

    public final String getName() {
        return this.program.getName();
    }

    public void addAuxTarget(String name, IntSupplier valueSupplier, int width, int height) {
        this.samplerNames.add(this.samplerNames.size(), name);
        this.samplerValues.add(this.samplerValues.size(), valueSupplier);
        this.samplerWidths.add(this.samplerWidths.size(), width);
        this.samplerHeights.add(this.samplerHeights.size(), height);
    }

    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public void render(float time) {
        this.input.endWrite();
        float f = this.output.textureWidth;
        float g = this.output.textureHeight;
        RenderSystem.viewport(0, 0, (int)f, (int)g);
        this.program.bindSampler("DiffuseSampler", this.input::getColorAttachment);
        for (int i = 0; i < this.samplerValues.size(); ++i) {
            this.program.bindSampler(this.samplerNames.get(i), this.samplerValues.get(i));
            this.program.getUniformByNameOrDummy("AuxSize" + i).set((float)this.samplerWidths.get(i).intValue(), (float)this.samplerHeights.get(i).intValue());
        }
        this.program.getUniformByNameOrDummy("ProjMat").set(this.projectionMatrix);
        this.program.getUniformByNameOrDummy("InSize").set((float)this.input.textureWidth, (float)this.input.textureHeight);
        this.program.getUniformByNameOrDummy("OutSize").set(f, g);
        this.program.getUniformByNameOrDummy("Time").set(time);
        MinecraftClient i = MinecraftClient.getInstance();
        this.program.getUniformByNameOrDummy("ScreenSize").set((float)i.getWindow().getFramebufferWidth(), (float)i.getWindow().getFramebufferHeight());
        this.program.enable();
        this.output.clear(MinecraftClient.IS_SYSTEM_MAC);
        this.output.beginWrite(false);
        RenderSystem.depthFunc(519);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(0.0, 0.0, 500.0).next();
        bufferBuilder.vertex(f, 0.0, 500.0).next();
        bufferBuilder.vertex(f, g, 500.0).next();
        bufferBuilder.vertex(0.0, g, 500.0).next();
        bufferBuilder.end();
        BufferRenderer.postDraw(bufferBuilder);
        RenderSystem.depthFunc(515);
        this.program.disable();
        this.output.endWrite();
        this.input.endRead();
        for (IntSupplier object : this.samplerValues) {
            if (!(object instanceof Framebuffer)) continue;
            ((Framebuffer)((Object)object)).endRead();
        }
    }

    public JsonEffectGlShader getProgram() {
        return this.program;
    }
}

