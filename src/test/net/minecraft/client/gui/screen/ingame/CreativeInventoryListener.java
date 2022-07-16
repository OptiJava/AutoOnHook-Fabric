/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;

@Environment(value=EnvType.CLIENT)
public class CreativeInventoryListener
implements ScreenHandlerListener {
    private final MinecraftClient client;

    public CreativeInventoryListener(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        this.client.interactionManager.clickCreativeStack(stack, slotId);
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
    }
}

