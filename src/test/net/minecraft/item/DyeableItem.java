/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface DyeableItem {
    public static final String COLOR_KEY = "color";
    public static final String DISPLAY_KEY = "display";
    public static final int DEFAULT_COLOR = 10511680;

    default public boolean hasColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        return nbtCompound != null && nbtCompound.contains(COLOR_KEY, 99);
    }

    default public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(COLOR_KEY, 99)) {
            return nbtCompound.getInt(COLOR_KEY);
        }
        return 10511680;
    }

    default public void removeColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(COLOR_KEY)) {
            nbtCompound.remove(COLOR_KEY);
        }
    }

    default public void setColor(ItemStack stack, int color) {
        stack.getOrCreateSubNbt(DISPLAY_KEY).putInt(COLOR_KEY, color);
    }

    public static ItemStack blendAndSetColor(ItemStack stack, List<DyeItem> colors) {
        int m;
        float h;
        ItemStack itemStack = ItemStack.EMPTY;
        int[] is = new int[3];
        int i = 0;
        int j = 0;
        DyeableItem dyeableItem = null;
        Item item = stack.getItem();
        if (item instanceof DyeableItem) {
            dyeableItem = (DyeableItem)((Object)item);
            itemStack = stack.copy();
            itemStack.setCount(1);
            if (dyeableItem.hasColor(stack)) {
                int k = dyeableItem.getColor(itemStack);
                float f = (float)(k >> 16 & 0xFF) / 255.0f;
                float g = (float)(k >> 8 & 0xFF) / 255.0f;
                h = (float)(k & 0xFF) / 255.0f;
                i = (int)((float)i + Math.max(f, Math.max(g, h)) * 255.0f);
                is[0] = (int)((float)is[0] + f * 255.0f);
                is[1] = (int)((float)is[1] + g * 255.0f);
                is[2] = (int)((float)is[2] + h * 255.0f);
                ++j;
            }
            for (DyeItem f : colors) {
                float[] g = f.getColor().getColorComponents();
                int h2 = (int)(g[0] * 255.0f);
                int l = (int)(g[1] * 255.0f);
                m = (int)(g[2] * 255.0f);
                i += Math.max(h2, Math.max(l, m));
                is[0] = is[0] + h2;
                is[1] = is[1] + l;
                is[2] = is[2] + m;
                ++j;
            }
        }
        if (dyeableItem == null) {
            return ItemStack.EMPTY;
        }
        int k = is[0] / j;
        int f = is[1] / j;
        int g = is[2] / j;
        h = (float)i / (float)j;
        float l = Math.max(k, Math.max(f, g));
        k = (int)((float)k * h / l);
        f = (int)((float)f * h / l);
        g = (int)((float)g * h / l);
        m = k;
        m = (m << 8) + f;
        m = (m << 8) + g;
        dyeableItem.setColor(itemStack, m);
        return itemStack;
    }
}

