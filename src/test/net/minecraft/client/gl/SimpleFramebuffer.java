/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;

@Environment(value=EnvType.CLIENT)
public class SimpleFramebuffer
extends Framebuffer {
    public SimpleFramebuffer(int width, int height, boolean useDepth, boolean getError) {
        super(useDepth);
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.resize(width, height, getError);
    }
}

