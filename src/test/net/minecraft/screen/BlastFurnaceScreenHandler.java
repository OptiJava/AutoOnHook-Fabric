/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;

public class BlastFurnaceScreenHandler
extends AbstractFurnaceScreenHandler {
    public BlastFurnaceScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.BLAST_FURNACE, RecipeType.BLASTING, RecipeBookCategory.BLAST_FURNACE, syncId, playerInventory);
    }

    public BlastFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerType.BLAST_FURNACE, RecipeType.BLASTING, RecipeBookCategory.BLAST_FURNACE, syncId, playerInventory, inventory, propertyDelegate);
    }
}

