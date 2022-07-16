/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.math;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

public interface BlockPointer
extends Position {
    @Override
    public double getX();

    @Override
    public double getY();

    @Override
    public double getZ();

    public BlockPos getPos();

    public BlockState getBlockState();

    public <T extends BlockEntity> T getBlockEntity();

    public ServerWorld getWorld();
}

