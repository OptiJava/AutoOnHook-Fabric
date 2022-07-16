/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ShieldDecorationRecipe
extends SpecialCraftingRecipe {
    public ShieldDecorationRecipe(Identifier identifier) {
        super(identifier);
    }

    @Override
    public boolean matches(CraftingInventory craftingInventory, World world) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = ItemStack.EMPTY;
        for (int i = 0; i < craftingInventory.size(); ++i) {
            ItemStack itemStack3 = craftingInventory.getStack(i);
            if (itemStack3.isEmpty()) continue;
            if (itemStack3.getItem() instanceof BannerItem) {
                if (!itemStack2.isEmpty()) {
                    return false;
                }
                itemStack2 = itemStack3;
                continue;
            }
            if (itemStack3.isOf(Items.SHIELD)) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                if (itemStack3.getSubNbt("BlockEntityTag") != null) {
                    return false;
                }
                itemStack = itemStack3;
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && !itemStack2.isEmpty();
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        Object itemStack3;
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = ItemStack.EMPTY;
        for (int i = 0; i < craftingInventory.size(); ++i) {
            itemStack3 = craftingInventory.getStack(i);
            if (((ItemStack)itemStack3).isEmpty()) continue;
            if (((ItemStack)itemStack3).getItem() instanceof BannerItem) {
                itemStack = itemStack3;
                continue;
            }
            if (!((ItemStack)itemStack3).isOf(Items.SHIELD)) continue;
            itemStack2 = ((ItemStack)itemStack3).copy();
        }
        if (itemStack2.isEmpty()) {
            return itemStack2;
        }
        NbtCompound i = itemStack.getSubNbt("BlockEntityTag");
        itemStack3 = i == null ? new NbtCompound() : i.copy();
        ((NbtCompound)itemStack3).putInt("Base", ((BannerItem)itemStack.getItem()).getColor().getId());
        itemStack2.setSubNbt("BlockEntityTag", (NbtElement)itemStack3);
        return itemStack2;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHIELD_DECORATION;
    }
}

