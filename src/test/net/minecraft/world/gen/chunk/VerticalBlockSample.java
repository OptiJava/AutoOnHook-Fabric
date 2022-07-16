/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public final class VerticalBlockSample {
    private final int startY;
    private final BlockState[] states;

    public VerticalBlockSample(int startY, BlockState[] states) {
        this.startY = startY;
        this.states = states;
    }

    public BlockState getState(BlockPos pos) {
        int i = pos.getY() - this.startY;
        if (i < 0 || i >= this.states.length) {
            return Blocks.AIR.getDefaultState();
        }
        return this.states[i];
    }
}

