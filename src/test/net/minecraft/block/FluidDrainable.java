/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public interface FluidDrainable {
    public ItemStack tryDrainFluid(WorldAccess var1, BlockPos var2, BlockState var3);

    /**
     * {@return the sound played when filling a bucket with the fluid contained in this block}
     * 
     * @see net.minecraft.fluid.Fluid#getBucketFillSound()
     */
    public Optional<SoundEvent> getBucketFillSound();
}

