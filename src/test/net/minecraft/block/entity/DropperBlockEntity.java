/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class DropperBlockEntity
extends DispenserBlockEntity {
    public DropperBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.DROPPER, blockPos, blockState);
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container.dropper");
    }
}

