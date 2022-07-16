/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShearsItem
extends Item {
    public ShearsItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && !state.isIn(BlockTags.FIRE)) {
            stack.damage(1, miner, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
        if (state.isIn(BlockTags.LEAVES) || state.isOf(Blocks.COBWEB) || state.isOf(Blocks.GRASS) || state.isOf(Blocks.FERN) || state.isOf(Blocks.DEAD_BUSH) || state.isOf(Blocks.HANGING_ROOTS) || state.isOf(Blocks.VINE) || state.isOf(Blocks.TRIPWIRE) || state.isIn(BlockTags.WOOL)) {
            return true;
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return state.isOf(Blocks.COBWEB) || state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(Blocks.TRIPWIRE);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (state.isOf(Blocks.COBWEB) || state.isIn(BlockTags.LEAVES)) {
            return 15.0f;
        }
        if (state.isIn(BlockTags.WOOL)) {
            return 5.0f;
        }
        if (state.isOf(Blocks.VINE) || state.isOf(Blocks.GLOW_LICHEN)) {
            return 2.0f;
        }
        return super.getMiningSpeedMultiplier(stack, state);
    }
}

