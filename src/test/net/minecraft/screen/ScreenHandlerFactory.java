/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ScreenHandlerFactory {
    @Nullable
    public ScreenHandler createMenu(int var1, PlayerInventory var2, PlayerEntity var3);
}

