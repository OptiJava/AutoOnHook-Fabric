/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.util;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.OrderedText;

@Environment(value=EnvType.CLIENT)
public interface OrderableTooltip {
    public List<OrderedText> getOrderedTooltip();
}

