/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TippedArrowItem
extends ArrowItem {
    public TippedArrowItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        return PotionUtil.setPotion(super.getDefaultStack(), Potions.POISON);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group)) {
            for (Potion potion : Registry.POTION) {
                if (potion.getEffects().isEmpty()) continue;
                stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        PotionUtil.buildTooltip(stack, tooltip, 0.125f);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return PotionUtil.getPotion(stack).finishTranslationKey(this.getTranslationKey() + ".effect.");
    }
}

