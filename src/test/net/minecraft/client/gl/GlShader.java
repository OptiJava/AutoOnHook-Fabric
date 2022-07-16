/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Program;

@Environment(value=EnvType.CLIENT)
public interface GlShader {
    public int getProgramRef();

    public void markUniformsDirty();

    public Program getVertexShader();

    public Program getFragmentShader();

    public void attachReferencedShaders();
}

