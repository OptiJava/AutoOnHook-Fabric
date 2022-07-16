/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import org.jetbrains.annotations.Nullable;

public class CommandBlockItem
extends BlockItem {
    public CommandBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        return playerEntity == null || playerEntity.isCreativeLevelTwoOp() ? super.getPlacementState(context) : null;
    }
}

