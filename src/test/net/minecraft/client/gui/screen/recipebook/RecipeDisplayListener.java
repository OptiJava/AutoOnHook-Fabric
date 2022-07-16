/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.recipe.Recipe;

@Environment(value=EnvType.CLIENT)
public interface RecipeDisplayListener {
    public void onRecipesDisplayed(List<Recipe<?>> var1);
}

