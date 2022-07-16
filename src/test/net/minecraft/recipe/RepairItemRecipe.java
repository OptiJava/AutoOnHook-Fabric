/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class RepairItemRecipe
extends SpecialCraftingRecipe {
    public RepairItemRecipe(Identifier identifier) {
        super(identifier);
    }

    @Override
    public boolean matches(CraftingInventory craftingInventory, World world) {
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < craftingInventory.size(); ++i) {
            ItemStack itemStack2;
            ItemStack itemStack = craftingInventory.getStack(i);
            if (itemStack.isEmpty()) continue;
            list.add(itemStack);
            if (list.size() <= 1 || itemStack.isOf((itemStack2 = (ItemStack)list.get(0)).getItem()) && itemStack2.getCount() == 1 && itemStack.getCount() == 1 && itemStack2.getItem().isDamageable()) continue;
            return false;
        }
        return list.size() == 2;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack i;
        Object itemStack2;
        ItemStack itemStack;
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i2 = 0; i2 < craftingInventory.size(); ++i2) {
            itemStack = craftingInventory.getStack(i2);
            if (itemStack.isEmpty()) continue;
            list.add(itemStack);
            if (list.size() <= 1 || itemStack.isOf(((ItemStack)(itemStack2 = (ItemStack)list.get(0))).getItem()) && ((ItemStack)itemStack2).getCount() == 1 && itemStack.getCount() == 1 && ((ItemStack)itemStack2).getItem().isDamageable()) continue;
            return ItemStack.EMPTY;
        }
        if (list.size() == 2 && (i = (ItemStack)list.get(0)).isOf((itemStack = (ItemStack)list.get(1)).getItem()) && i.getCount() == 1 && itemStack.getCount() == 1 && i.getItem().isDamageable()) {
            itemStack2 = i.getItem();
            int j = ((Item)itemStack2).getMaxDamage() - i.getDamage();
            int k = ((Item)itemStack2).getMaxDamage() - itemStack.getDamage();
            int l = j + k + ((Item)itemStack2).getMaxDamage() * 5 / 100;
            int m = ((Item)itemStack2).getMaxDamage() - l;
            if (m < 0) {
                m = 0;
            }
            ItemStack itemStack3 = new ItemStack(i.getItem());
            itemStack3.setDamage(m);
            HashMap<Enchantment, Integer> map = Maps.newHashMap();
            Map<Enchantment, Integer> map2 = EnchantmentHelper.get(i);
            Map<Enchantment, Integer> map3 = EnchantmentHelper.get(itemStack);
            Registry.ENCHANTMENT.stream().filter(Enchantment::isCursed).forEach(enchantment -> {
                int i = Math.max(map2.getOrDefault(enchantment, 0), map3.getOrDefault(enchantment, 0));
                if (i > 0) {
                    map.put((Enchantment)enchantment, i);
                }
            });
            if (!map.isEmpty()) {
                EnchantmentHelper.set(map, itemStack3);
            }
            return itemStack3;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}

