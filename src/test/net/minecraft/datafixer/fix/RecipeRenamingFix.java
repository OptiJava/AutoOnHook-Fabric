/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.datafixer.fix.RecipeRenameFix;

public class RecipeRenamingFix
extends RecipeRenameFix {
    private static final Map<String, String> RECIPES = ImmutableMap.builder().put("minecraft:acacia_bark", "minecraft:acacia_wood").put("minecraft:birch_bark", "minecraft:birch_wood").put("minecraft:dark_oak_bark", "minecraft:dark_oak_wood").put("minecraft:jungle_bark", "minecraft:jungle_wood").put("minecraft:oak_bark", "minecraft:oak_wood").put("minecraft:spruce_bark", "minecraft:spruce_wood").build();

    public RecipeRenamingFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "Recipes renamening fix", string -> RECIPES.getOrDefault(string, (String)string));
    }
}
