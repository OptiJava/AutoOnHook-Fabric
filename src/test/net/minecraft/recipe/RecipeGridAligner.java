/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.recipe;

import java.util.Iterator;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.math.MathHelper;

public interface RecipeGridAligner<T> {
    default public void alignRecipeToGrid(int gridWidth, int gridHeight, int gridOutputSlot, Recipe<?> recipe, Iterator<T> inputs, int amount) {
        int i = gridWidth;
        int j = gridHeight;
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
            i = shapedRecipe.getWidth();
            j = shapedRecipe.getHeight();
        }
        int shapedRecipe = 0;
        block0: for (int k = 0; k < gridHeight; ++k) {
            if (shapedRecipe == gridOutputSlot) {
                ++shapedRecipe;
            }
            boolean bl = (float)j < (float)gridHeight / 2.0f;
            int l = MathHelper.floor((float)gridHeight / 2.0f - (float)j / 2.0f);
            if (bl && l > k) {
                shapedRecipe += gridWidth;
                ++k;
            }
            for (int m = 0; m < gridWidth; ++m) {
                boolean bl2;
                if (!inputs.hasNext()) {
                    return;
                }
                bl = (float)i < (float)gridWidth / 2.0f;
                l = MathHelper.floor((float)gridWidth / 2.0f - (float)i / 2.0f);
                int n = i;
                boolean bl3 = bl2 = m < i;
                if (bl) {
                    n = l + i;
                    boolean bl4 = bl2 = l <= m && m < l + i;
                }
                if (bl2) {
                    this.acceptAlignedInput(inputs, shapedRecipe, amount, k, m);
                } else if (n == m) {
                    shapedRecipe += gridWidth - m;
                    continue block0;
                }
                ++shapedRecipe;
            }
        }
    }

    public void acceptAlignedInput(Iterator<T> var1, int var2, int var3, int var4, int var5);
}

