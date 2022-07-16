/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.surfacebuilder;

import net.minecraft.block.BlockState;

public interface SurfaceConfig {
    public BlockState getTopMaterial();

    public BlockState getUnderMaterial();

    public BlockState getUnderwaterMaterial();
}

