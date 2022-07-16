/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FireworkStarItem
extends Item {
    public FireworkStarItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbtCompound = stack.getSubNbt("Explosion");
        if (nbtCompound != null) {
            FireworkStarItem.appendFireworkTooltip(nbtCompound, tooltip);
        }
    }

    public static void appendFireworkTooltip(NbtCompound nbt, List<Text> tooltip) {
        int[] js;
        FireworkRocketItem.Type type = FireworkRocketItem.Type.byId(nbt.getByte("Type"));
        tooltip.add(new TranslatableText("item.minecraft.firework_star.shape." + type.getName()).formatted(Formatting.GRAY));
        int[] is = nbt.getIntArray("Colors");
        if (is.length > 0) {
            tooltip.add(FireworkStarItem.appendColors(new LiteralText("").formatted(Formatting.GRAY), is));
        }
        if ((js = nbt.getIntArray("FadeColors")).length > 0) {
            tooltip.add(FireworkStarItem.appendColors(new TranslatableText("item.minecraft.firework_star.fade_to").append(" ").formatted(Formatting.GRAY), js));
        }
        if (nbt.getBoolean("Trail")) {
            tooltip.add(new TranslatableText("item.minecraft.firework_star.trail").formatted(Formatting.GRAY));
        }
        if (nbt.getBoolean("Flicker")) {
            tooltip.add(new TranslatableText("item.minecraft.firework_star.flicker").formatted(Formatting.GRAY));
        }
    }

    private static Text appendColors(MutableText line, int[] colors) {
        for (int i = 0; i < colors.length; ++i) {
            if (i > 0) {
                line.append(", ");
            }
            line.append(FireworkStarItem.getColorText(colors[i]));
        }
        return line;
    }

    private static Text getColorText(int color) {
        DyeColor dyeColor = DyeColor.byFireworkColor(color);
        if (dyeColor == null) {
            return new TranslatableText("item.minecraft.firework_star.custom_color");
        }
        return new TranslatableText("item.minecraft.firework_star." + dyeColor.getName());
    }
}

