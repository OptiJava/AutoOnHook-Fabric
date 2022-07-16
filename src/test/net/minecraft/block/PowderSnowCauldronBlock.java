/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class PowderSnowCauldronBlock
extends LeveledCauldronBlock {
    public PowderSnowCauldronBlock(AbstractBlock.Settings settings, Predicate<Biome.Precipitation> predicate, Map<Item, CauldronBehavior> map) {
        super(settings, predicate, map);
    }

    @Override
    protected void onFireCollision(BlockState state, World world, BlockPos pos) {
        PowderSnowCauldronBlock.decrementFluidLevel((BlockState)Blocks.WATER_CAULDRON.getDefaultState().with(LEVEL, state.get(LEVEL)), world, pos);
    }
}

