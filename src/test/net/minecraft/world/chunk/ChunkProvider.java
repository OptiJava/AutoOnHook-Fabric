/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.chunk;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;

public interface ChunkProvider {
    @Nullable
    public BlockView getChunk(int var1, int var2);

    default public void onLightUpdate(LightType type, ChunkSectionPos pos) {
    }

    public BlockView getWorld();
}

